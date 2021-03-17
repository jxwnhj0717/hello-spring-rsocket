package hello.spring.rsocket.client;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.Resume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.Disposable;

import java.util.UUID;

@Slf4j
@ShellComponent
public class RSocketShellClient  {

    static final String CLIENT = "Client";
    static final String REQUEST = "Request";
    static final String FIRE_AND_FORGET = "fire-and-forget";
    static final String STREAM = "Stream";
    static final String CHANNEL = "Channel";

    private final RSocketRequester rsocketRequester;

//    @Autowired
//    public RSocketShellClient(RSocketRequester.Builder rsocketRequesterBuilder) {
//        this.rsocketRequester = rsocketRequesterBuilder.tcp("localhost", 7000);
//    }

    public RSocketShellClient(RSocketRequester.Builder rsocketRequesterBuilder, RSocketStrategies strategies) {
        String client = UUID.randomUUID().toString();
        log.info("Connecting using client ID: {}", client);

        SocketAcceptor acceptor = RSocketMessageHandler.responder(strategies, new ClientHandler());

        this.rsocketRequester = rsocketRequesterBuilder.setupRoute("shell-client")
                .setupData(client)
                .rsocketStrategies(strategies)
                .rsocketConnector(connector -> connector.acceptor(acceptor).resume(new Resume()))
                .connectTcp("localhost", 7000)
                .block();
                //.tcp("localhost", 7000);

        this.rsocketRequester.rsocket()
                .onClose()
                .doOnError(er -> log.error("connection closed.", er))
                .doFinally(c -> log.info("connection disconnected"))
                .subscribe();

    }

    @ShellMethod("Send one request. One response will be printed.")
    public void requestResponse() throws InterruptedException {
        log.info("\nSending one request. Waiting for one response...");
        Message message = this.rsocketRequester
                .route("request-response")
                .data(new Message(CLIENT, REQUEST))
                .retrieveMono(Message.class)
                .block();
        log.info("\nResponse was: {}", message);
//        this.rsocketRequester
//                .route("request-response")
//                .data(new Message(CLIENT, FIRE_AND_FORGET))
//                .send()
//                .log()
//                .block();
    }

    @ShellMethod("Send one request. No response will be returned.")
    public void fireAndForget() throws InterruptedException {
        log.info("\nFire-And-Forget. Sending one request. Expect no response (check server log)...");
        this.rsocketRequester
                .route("fire-and-forget")
                .data(new Message(CLIENT, FIRE_AND_FORGET))
                .send()
                .block();
    }

    private Disposable disposable;


    @ShellMethod("Send one request. Many responses (stream) will be printed.")
    public void stream() {
        log.info("\nRequest-Stream. Sending one request. Waiting for unlimited responses (Stop process to quit)...");
        this.disposable = this.rsocketRequester
                .route("stream")
                .data(new Message(CLIENT, STREAM))
                .retrieveFlux(Message.class)
                .subscribe(e -> log.info("Received {}", e));
    }


    @ShellMethod("Stop streaming messages from the server.")
    public void st() {
        if(disposable != null) {
            disposable.dispose();
        }
    }

}
