package com.example.outbox.stream;

import com.example.outbox.Event;
import com.example.outbox.model.Order;
import com.example.outbox.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockStream {
    private final KafkaReceiver<String, Event> receiver;
    private final StockService service;
    private final ObjectMapper mapper;

    enum EventType {
        ORDER_CREATED,
        ORDER_PROCESSING,
        ORDER_REJECTED,
        ORDER_CANCELLED
    }

    @PostConstruct
    public void subscribe() {
        receiver.receive()
                .delayUntil(record -> handle(record)
                        .doOnSuccess(o -> record.receiverOffset().acknowledge())
                        .doOnError(e -> log.error("[{}] error occurred => {}", record.key(), e.getMessage()))
                )
                .subscribe();
    }

    @PreDestroy
    public void destroy() {
        log.warn("Shutting down stream ...");
    }

    private Mono<Void> handle(ReceiverRecord<String, Event> record) {
        log.info("Received message from topic: {}, key:{}, value:{}", record.topic(), record.key(), record.value());
        String eventType = record.value().getEventType().toString().toUpperCase();
        switch (EventType.valueOf(eventType)) {
            case ORDER_PROCESSING:
                log.info("Handle event_type: {}", eventType);
                String payload = record.value().getPayload().toString();
                return Mono.fromCallable(() -> mapper.readValue(payload, Order.class))
                        .flatMap(o -> service.allocateStock(o))
                        .then();
            default:
                log.info("Un-handle event_type: {}", eventType);
        }
        return Mono.empty();
    }
}
