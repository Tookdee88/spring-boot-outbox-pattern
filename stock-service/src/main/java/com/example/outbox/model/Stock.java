package com.example.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    private String barcode;
    private Integer qty;
}
