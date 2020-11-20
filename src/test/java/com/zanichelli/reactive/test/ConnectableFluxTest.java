package com.zanichelli.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@Slf4j
public class ConnectableFluxTest {

    @Test
    public void connectableFlux() throws InterruptedException {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish();
/*
        connectableFlux.connect();

        log.info("Thread sleeping for 300ms");
        Thread.sleep(300);

        connectableFlux.subscribe(i -> log.info("1. Number {}", i));

        log.info("Thread sleeping for 200ms");
        Thread.sleep(200);

        connectableFlux.subscribe(i -> log.info("2. Number {}", i));
*/
        StepVerifier
                .create(connectableFlux)
                .then(connectableFlux::connect)
                //.thenConsumeWhile(i -> i <= 5)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .expectComplete()
                .verify();
    }
}
