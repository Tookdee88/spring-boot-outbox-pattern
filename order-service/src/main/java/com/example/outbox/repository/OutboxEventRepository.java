package com.example.outbox.repository;

import com.example.outbox.model.OutboxEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OutboxEventRepository extends ReactiveMongoRepository<OutboxEvent, String> {
}
