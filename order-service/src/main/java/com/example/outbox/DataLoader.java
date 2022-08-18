package com.example.outbox;

import com.example.outbox.model.Order;
import com.example.outbox.model.OrderStatus;
import com.example.outbox.model.OutboxEvent;
import com.example.outbox.repository.OrderRepository;
import com.example.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
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
        List<Order> orders = IntStream.range(1, 20).mapToObj(i -> Order.builder()
                .orderNo(String.format("OD-%03d", i))
                .orderDate(new Date())
                .deliveryDate(new Date())
                .storeCode(String.format("TH-%03d", i))
                .storeName(String.format("ร้านค้า-%03d", i))
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

        // Dummy for auto create collection
        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(OrderStatus.NEW.getEventType())
                .aggregateType(OUTBOX_AGGREGATE_TYPE)
                .payload("{}")
                .build();

        Flux.fromIterable(orders)
                .flatMap(o -> orderRepository.save(o))
                .then(outboxEventRepository.save(event))
                .subscribe();
    }
}
