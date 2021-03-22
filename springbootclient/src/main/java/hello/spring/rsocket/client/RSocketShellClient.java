package hello.spring.rsocket.client;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.Resume;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;

import java.util.UUID;

@Slf4j
@ShellComponent
public class RSocketShellClient implements ApplicationContextAware {

    static final String REQUEST = "Request";
    static final String FIRE_AND_FORGET = "fire-and-forget";
    static final String STREAM = "Stream";

    private ApplicationContext applicationContext;

    @Getter
    @Setter
    private String clientId;
    private RSocketRequester rsocketRequester;

    @ShellMethod("connect to server.")
    public void connect(@ShellOption(defaultValue="") String id) {
        if(StringUtils.hasLength(clientId)) {
            log.info("connection established.");
            return;
        }
        if(!StringUtils.hasLength(id)) {
            id = UUID.randomUUID().toString();
        }
        log.info("Connecting using client ID: {}", id);

        RSocketRequester.Builder rsocketRequesterBuilder = applicationContext.getBean(RSocketRequester.Builder.class);
        RSocketStrategies strategies = applicationContext.getBean(RSocketStrategies.class);

        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setApplicationContext(applicationContext);
        handler.setRSocketStrategies(strategies);
        handler.afterPropertiesSet();
        SocketAcceptor acceptor = handler.responder();

        if(this.rsocketRequester != null) {
            rsocketRequester.rsocket().dispose();
        }

        RSocketRequester realRSocketRequester = rsocketRequesterBuilder.setupRoute("shell-client")
                .setupData(new Message(id, "connect"))
                .rsocketStrategies(strategies)
                .rsocketConnector(connector -> connector.acceptor(acceptor).resume(new Resume()))
                .connectTcp("localhost", 7000)
                .block();
        this.rsocketRequester = new RSocketClientRequester(id, realRSocketRequester);

        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(er -> log.error("connection closed.", er))
                .doFinally(c -> log.info("connection disconnected"))
                .subscribe();
    }


    @ShellMethod("Send one request. One response will be printed.")
    public void requestResponse() throws InterruptedException {
        log.info("\nSending one request. Waiting for one response...");
        this.rsocketRequester
                .route("request-response")
                .data(new Message(clientId, REQUEST))
                .retrieveMono(Message.class)
                .subscribe(m -> {
                    log.info("\nResponse was: {}", m);
                });
    }

    @ShellMethod("Send one request. No response will be returned.")
    public void fireAndForget() throws InterruptedException {
        log.info("\nFire-And-Forget. Sending one request. Expect no response (check server log)...");
        this.rsocketRequester
                .route("fire-and-forget")
                .data(new Message(clientId, FIRE_AND_FORGET))
                .send()
                .doOnSuccess(e -> log.info("\nSent successfully."))
                .subscribe();
    }

    private Disposable disposable;


    @ShellMethod("Send one request. Many responses (stream) will be printed.")
    public void stream() {
        log.info("\nRequest-Stream. Sending one request. Waiting for unlimited responses (Stop process to quit)...");
        this.disposable = this.rsocketRequester
                .route("stream")
                .data(new Message(clientId, STREAM))
                .retrieveFlux(Message.class)
                .subscribe(e -> log.info("Received {}", e));
    }


    @ShellMethod("Stop streaming messages from the server.")
    public void st() {
        if(disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
