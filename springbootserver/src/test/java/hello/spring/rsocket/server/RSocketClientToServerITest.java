package hello.spring.rsocket.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RSocketClientToServerITest {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder,
                                 @Value("${spring.rsocket.server.port}") Integer port) {
        requester = builder.tcp("localhost", port);
    }

    @AfterAll
    public static void tearDown() {
        requester.rsocketClient().dispose();
    }

    @Test
    public void testRequestGetsResponse() {
        Mono<Message> msg = requester.route("request-response")
                .data(new Message("Test", "Request"))
                .retrieveMono(Message.class);

        StepVerifier.create(msg)
                .consumeNextWith(e -> {
                    assertEquals(e.getOrigin(), RSocketController.SERVER);
                }).verifyComplete();
    }
}
