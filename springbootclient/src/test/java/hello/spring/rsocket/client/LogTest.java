package hello.spring.rsocket.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class LogTest {

    @Test void subscribeOn() {
        handleMessage("hj").subscribeOn(Schedulers.parallel())
                .subscribe(e -> log.info("hello:" + e));
    }

    public Mono<String> handleMessage(String value) {
        return Mono.just(1).map(i -> {
            log.info("invoke: " + value);
            return value;
        });
    }
}
