package com.example.outbox.repository;

import com.example.outbox.model.Stock;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface StockRepository extends ReactiveMongoRepository<Stock, String>  {
    Mono<Stock> findByBarcode(String barcode);
}
