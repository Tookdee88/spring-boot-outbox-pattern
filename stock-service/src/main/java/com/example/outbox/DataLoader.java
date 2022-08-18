package com.example.outbox;

import com.example.outbox.model.Stock;
import com.example.outbox.model.StockOut;
import com.example.outbox.repository.StockOutRepository;
import com.example.outbox.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
    private final StockRepository stockRepository;
    private final StockOutRepository stockOutRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Stock> stocks = IntStream.range(1, 20).mapToObj(i -> Stock.builder()
                    .barcode(String.format("%03d", i))
                    .qty(50000)
                    .build()
        ).toList();

        // Dummy for auto create collection
        StockOut stockOut = StockOut.builder()
                .orderNo("X-001")
                .barcode("999")
                .qty(0)
                .build();

        log.info("Clean up sample data...");
        stockRepository.deleteAll()
                .then(stockOutRepository.deleteAll())
                .subscribe();

        log.info("Loading new sample data...");
        Flux.fromIterable(stocks)
                .flatMap(o -> stockRepository.save(o))
                .then(stockOutRepository.save(stockOut))
                .subscribe();
    }
}
