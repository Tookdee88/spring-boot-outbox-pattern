package com.example.outbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stock_outs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOut {
    private String orderNo;
    private String barcode;
    private Integer qty;
}
