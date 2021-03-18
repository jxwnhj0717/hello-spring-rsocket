package hello.spring.rsocket.server;

import lombok.extern.slf4j.Slf4j;
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
    static final String CHANNEL = "Channel";

    @MessageMapping("request-response")
    Message requestResponse(Message request) {
        log.info("Received request-response request: {}", request);
        // create a single Message and return it
        return new Message(SERVER, RESPONSE);
    }

    @MessageMapping("fire-and-forget")
    public void fireAndForget(Message request) {
        log.info("Received fire-and-forget request: {}", request);
    }

    @MessageMapping("stream")
    Flux<Message> stream(Message request) {
        log.info("Received stream request: {}", request);
        return Flux
                .interval(Duration.ofSeconds(1))
                .map(index -> new Message(SERVER, STREAM, index))
                .log();
    }

    @ConnectMapping("shell-client")
    void connectShellClientAndAskForTelemetry(RSocketRequester requester, @Payload String client) {
        requester.rsocket()
                .onClose()
                .doFirst(() -> {
                    log.info("Client: {} CONNECTED.", client);
                    RSocketRequester oldRequester = clientMap.put(client, requester);
                    if(oldRequester != null) {
                        oldRequester.rsocket().dispose();
                    }
                })
                .doOnError(error -> {
                    log.warn("Channel to client {} CLOSED", client);
                })
                .doFinally(consumer -> {
                    clientMap.remove(client);
                    log.info("Client {} DISCONNECTED", client);
                })
                .subscribe();

        requester.route("client-status")
                .data(client)
                .retrieveFlux(String.class)
                .doOnNext(s -> log.info("client: {} Free Memory: {}.", client, s))
                .subscribe();
    }
}
