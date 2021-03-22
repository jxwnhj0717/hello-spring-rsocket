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
    public Flux<String> statusUpdate(Message message) {
        log.info("Connection[{}] OPEN", message.getValue());
        this.client.setClientId(message.getValue());
        return Flux.interval(Duration.ofSeconds(5))
                .map(i -> String.valueOf(Runtime.getRuntime().freeMemory()));
    }
}
