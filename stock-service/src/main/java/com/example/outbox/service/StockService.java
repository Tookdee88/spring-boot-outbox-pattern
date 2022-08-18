package com.example.outbox.service;

import com.example.outbox.model.Order;
import com.example.outbox.model.StockOut;
import com.example.outbox.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {
    private final StockRepository repository;
    private final ReactiveMongoTemplate template;

    public Mono<Void> allocateStock(Order order) {
        log.info("Allocating stock of order: {}", order.getOrderNo());
        return Flux.fromIterable(order.getItems())
                .flatMap(i -> repository.findByBarcode(i.getBarcode())
                        .flatMap(stock -> {
                            int alloc_qty = (stock.getQty() >= i.getQty()) ? i.getQty() : stock.getQty();
                            int avail_qty = stock.getQty() - alloc_qty;
                            StockOut stockOut = StockOut.builder()
                                    .orderNo(order.getOrderNo())
                                    .barcode(i.getBarcode())
                                    .qty(alloc_qty)
                                    .build();
                            stock.setQty(avail_qty);
                            log.info("- barcode: {}, qty: {}, alloc_qty: {}, avail_qty: {}", i.getBarcode(), i.getQty(), alloc_qty, avail_qty);
                            return template.inTransaction().execute(action ->
                                    action.insert(stockOut).then(action.save(stock))
                            ).next();
                        })
                ).then();
    }
}
