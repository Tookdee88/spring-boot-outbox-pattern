package com.example.outbox.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus implements StringEnum {
    NEW("NEW", "order_created"),
    PROCESSING("PROCESSING", "order_processing"),
    REJECTED("REJECTED", "order_rejected"),
    CANCELLED("CANCELLED", "order_cancelled");

    private String value;
    private String eventType;

    @Override
    @JsonValue
    public String value() {
        return this.value;
    }
}
