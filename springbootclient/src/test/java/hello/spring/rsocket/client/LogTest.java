package hello.spring.rsocket.client;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class LogTest {

    public static void main(String[] args) throws InterruptedException {
        Flux.interval(Duration.ofSeconds(1))
                .log("A")
                .map(i -> 'a' + i)
                .log("B")
                .map(c -> "hello:" + c)
                .log("C")
                .subscribe(System.out::println);
        Thread.sleep(10000);

    }
}
