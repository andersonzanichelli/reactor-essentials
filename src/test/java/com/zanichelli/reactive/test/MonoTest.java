package com.zanichelli.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * 1. Publisher sends all the objects requested. (Send cakes with backpressure)
 * 2. Publisher sends all the objects it has. (onComplete) subscriber and subscription will be cancelled.
 * 3. There is an error. (onError) -> subscriber and subscription will be cancelled
 */
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "Anderson Zanichelli";
        Mono<String> mono = Mono.just(name).log();

        mono.subscribe(s -> log.info(s.toLowerCase()));

        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext("Anderson Zanichelli")
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Anderson Zanichelli";
        Mono<String> mono = Mono.just(name)
                .map(s -> { throw new RuntimeException("Testing mono with error"); });

        mono.subscribe(s -> log.info(s.toLowerCase()), Throwable::printStackTrace);
        mono.subscribe(s -> log.info(s.toLowerCase()), s -> log.error("Something bad happened!"));

        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "Anderson Zanichelli";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("Finished!"));

        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext("Anderson Zanichelli")
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "Anderson Zanichelli";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("Finished!"),
                subscription -> subscription.request(5));

        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext("ANDERSON ZANICHELLI")
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Anderson Zanichelli";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed!"))
                .doOnRequest(number -> log.info("Request received, start doing something"))
                .doOnNext(s -> log.info("Value {}. Executing do onNext", s))
                .doOnSuccess(s -> log.info("doOnSuccess executed!"));

        mono.subscribe(s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("Finished!"));

        log.info("-------------------------");
        StepVerifier.create(mono)
                .expectNext("ANDERSON ZANICHELLI")
                .verifyComplete();

    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "Anderson Zanichelli";

        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Inside on error resume");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext("Anderson Zanichelli")
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "Anderson Zanichelli";

        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> MonoTest.log.error("Error message: {}", e.getMessage()))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Not executed!!");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }
}
