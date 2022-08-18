package com.example.outbox.service;

import com.example.outbox.model.Order;
import com.example.outbox.model.OutboxEvent;
import com.example.outbox.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private static final String OUTBOX_AGGREGATE_TYPE = "order";
    private final OrderRepository repository;
    private final ReactiveMongoTemplate template;
    private final ObjectMapper mapper;

    public Mono<Order> findByOrderNo(String orderNo) {
        log.info("find order_no: ", orderNo);
        return repository.findByOrderNo(orderNo);
    }

    public Mono<Order> saveOrder(Order order) {
        try {
            final OutboxEvent event = OutboxEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(order.getStatus().getEventType())
                    .aggregateType(OUTBOX_AGGREGATE_TYPE)
                    .payload(mapper.writeValueAsString(order))
                    .build();
            log.info("fire outbox_event: {}", event);
            if (order.getId() != null) {
                return template.inTransaction().execute(action ->
                                action.save(order).zipWith(action.insert(event)))
                        .map(tuple -> tuple.getT1())
                        .next();
            }
            return template.inTransaction().execute(action ->
                            action.insert(order).zipWith(action.insert(event)))
                    .map(tuple -> tuple.getT1())
                    .next();
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
