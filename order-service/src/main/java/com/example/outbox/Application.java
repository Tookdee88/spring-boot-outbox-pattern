package com.example.outbox;

import com.example.outbox.handler.OrderHandler;
import com.example.outbox.model.Order;
import com.example.outbox.model.OrderStatus;
import com.example.outbox.model.OutboxEvent;
import com.example.outbox.repository.OrderRepository;
import com.example.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    class Router {
        @Bean
        public RouterFunction<ServerResponse> stockRouters(OrderHandler handler) {
            return RouterFunctions.route(POST("/orders").and(accept(APPLICATION_JSON)), handler::createOrder)
                    .andRoute(PATCH("/orders/{orderNo}/{status}").and(accept(APPLICATION_JSON)), handler::updateOrder)
                    .andRoute(GET("/orders/{orderNo}").and(accept(APPLICATION_JSON)), handler::getOrder);
        }
    }

    @Component
    @RequiredArgsConstructor
    class DataLoader implements CommandLineRunner {
        private static final String OUTBOX_AGGREGATE_TYPE = "order";
        private final OrderRepository orderRepository;
        private final OutboxEventRepository outboxEventRepository;

        @Override
        public void run(String... args) throws Exception {
            log.info("Clean up sample data...");
            orderRepository.deleteAll()
                    .then(outboxEventRepository.deleteAll())
                    .subscribe();

            log.info("Loading new sample data...");
            List<Order> orders = IntStream.range(1, 20000).mapToObj(i -> Order.builder()
                    .orderNo(String.format("OD-%06d", i))
                    .orderDate(new Date())
                    .deliveryDate(new Date())
                    .storeCode(String.format("TH-%06d", i))
                    .storeName(String.format("ร้านค้า-%06d", i))
                    .status(OrderStatus.NEW)
                    .items(Arrays.asList(
                            Order.Item.builder()
                                    .barcode("001")
                                    .qty(20)
                                    .build(),
                            Order.Item.builder()
                                    .barcode("002")
                                    .qty(50)
                                    .build()))
                    .build()
            ).toList();

            Flux.fromIterable(orders)
                    .flatMap(o -> orderRepository.save(o))
                    .flatMap(o -> outboxEventRepository.save(OutboxEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .eventType(OrderStatus.NEW.getEventType())
                            .aggregateType(OUTBOX_AGGREGATE_TYPE)
                            .payload("{}")
                            .build()))
                    .subscribe();
        }
    }
}
