package dev.vality.magista.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.vality.magista.domain.tables.pojos.InvoiceData;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    Cache<String, InvoiceData> invoiceDataCache(@Value("${cache.invoiceData.size}") int cacheSize) {
        return Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();
    }

    @Bean
    Cache<Map.Entry<String, String>, PaymentData> paymentDataCache(@Value("${cache.paymentData.size}") int cacheSize) {
        return Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();
    }

}
