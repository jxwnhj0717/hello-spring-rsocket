package hello.spring.rsocket.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Controller
public class ClientHandler {

    @Autowired
    private RSocketShellClient client;

    @MessageMapping("client-status")
    public Flux<String> statusUpdate(String clientId) {
        log.info("Connection[{}] OPEN", clientId);
        this.client.setClientId(clientId);
        return Flux.interval(Duration.ofSeconds(5))
                .map(i -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }
}
