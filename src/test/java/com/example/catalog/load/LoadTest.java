package com.example.catalog.load;

import com.example.catalog.CatalogServiceApplication;
import com.example.catalog.dto.ProductCreateDto;
import com.example.catalog.entity.ProductType;
import com.example.catalog.event.ContentIndexEvent;
import com.example.catalog.repository.elasticsearch.ProductElasticsearchRepository;
import com.example.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = CatalogServiceApplication.class)
@ActiveProfiles("test")
class LoadTest {

    @MockBean
    private ProductElasticsearchRepository elasticsearchRepository;

    @MockBean
    private KafkaTemplate<String, ContentIndexEvent> kafkaTemplate;

    @Autowired
    private ProductService service;

    @Test
    void loadTestCreate() throws InterruptedException {
        int threadCount = 10;
        int requestsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        ProductCreateDto dto = new ProductCreateDto();
                        dto.setTitle("Load Test " + System.nanoTime());
                        dto.setDescription("Description");
                        dto.setType(ProductType.EBOOK);
                        dto.setPriceCents(100);
                        dto.setCurrency("USD");
                        service.create(dto);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                        failureCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        System.out.println("Load Test Results:");
        System.out.println("Total Requests: " + (threadCount * requestsPerThread));
        System.out.println("Successful: " + successCount.get());
        System.out.println("Failed: " + failureCount.get());
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
        
        assertEquals(threadCount * requestsPerThread, successCount.get());
        assertEquals(0, failureCount.get());
    }
}
