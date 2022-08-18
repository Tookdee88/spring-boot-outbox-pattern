package com.example.outbox.repository;

import com.example.outbox.model.StockOut;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface StockOutRepository extends ReactiveMongoRepository<StockOut, String>  {
}
