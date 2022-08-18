package com.example.outbox.handler;

import com.example.outbox.model.Order;
import com.example.outbox.model.OrderStatus;
import com.example.outbox.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@RequiredArgsConstructor
public class OrderHandler {
    private final OrderService service;

    public Mono<ServerResponse> getOrder(ServerRequest request) {
        return service.findByOrderNo(request.pathVariable("orderNo"))
                .flatMap(o -> ok().contentType(APPLICATION_JSON).body(fromValue(o)));
    }

    public Mono<ServerResponse> createOrder(ServerRequest request) {
        return request.bodyToMono(Order.class)
                .flatMap(o -> service.saveOrder(o))
                .flatMap(o -> ok().contentType(APPLICATION_JSON).body(fromValue(o)));
    }

    public Mono<ServerResponse> updateOrder(ServerRequest request) {
        return service.findByOrderNo(request.pathVariable("orderNo"))
                .flatMap(o -> {
                    o.setStatus(OrderStatus.valueOf(request.pathVariable("status").toUpperCase()));
                    return service.saveOrder(o);
                })
                .flatMap(o -> ok().contentType(APPLICATION_JSON).body(fromValue(o)));
    }
}
