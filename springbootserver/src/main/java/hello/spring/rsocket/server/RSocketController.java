package hello.spring.rsocket.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class RSocketController {

    private final Map<String, RSocketRequester> clientMap = new ConcurrentHashMap<>();

    static final String SERVER = "Server";
    static final String RESPONSE = "Response";
    static final String STREAM = "Stream";

    @MessageMapping("request-response")
    Message requestResponse(String clientId, Message request) {
        log.info("Client[{}] request-response request: {}", clientId, request);
        return new Message(SERVER, RESPONSE);
    }

    @MessageMapping("fire-and-forget")
    public void fireAndForget(String clientId, Message request) {
        log.info("Client[{}] fire-and-forget request: {}", clientId, request);
    }

    @MessageMapping("stream")
    Flux<Message> stream(String clientId, Message request) {
        log.info("Client[{}] stream request: {}", clientId, request);
        return Flux
                .interval(Duration.ofSeconds(1))
                .map(index -> new Message(SERVER, STREAM, index))
                .log();
    }

    @ConnectMapping("shell-client")
    void connectShellClientAndAskForTelemetry(RSocketRequester requester, @Payload Message message) {
        requester.rsocket()
                .onClose()
                .doFirst(() -> {
                    log.info("Client: {} CONNECTED.", message.getOrigin());
                    RSocketRequester oldRequester = clientMap.put(message.getOrigin(), requester);
                    if(oldRequester != null) {
                        oldRequester.rsocket().dispose();
                    }
                })
                .doOnError(error -> {
                    log.warn("Channel to client {} CLOSED", message.getOrigin());
                })
                .doFinally(consumer -> {
                    clientMap.remove(message.getOrigin());
                    log.info("Client {} DISCONNECTED", message.getOrigin());
                })
                .subscribe();

        requester.route("client-status")
                .data(new Message("server", message.getOrigin()))
                .retrieveFlux(String.class)
                .publishOn(SchedulerPool.getInstance().getScheduler(message.getOrigin()))
                .doOnNext(s -> log.info("client: {} Free Memory: {}.", message.getOrigin(), s))
                .subscribe();
    }
}
