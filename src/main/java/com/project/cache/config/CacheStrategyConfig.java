package com.project.cache.config;

import com.project.cache.strategy.CacheStrategy;
import com.project.cache.strategy.CacheStrategyFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Slf4j
public class CacheStrategyConfig {

    @Value("${app.cache.strategy:cacheAside}")
    private String configuredStrategy;

    @Autowired
    @Lazy
    private CacheStrategyFactory strategyFactory;

    private CacheStrategy selectedStrategy;

    @PostConstruct
    public void initializeStrategy() {
        try {
            selectedStrategy = strategyFactory.getStrategy(configuredStrategy);
            if (!selectedStrategy.isStrategyAvailable()) {
                selectedStrategy = strategyFactory.getCacheAsideStrategy();
            }
            log.info("Initialized cache strategy: {}", selectedStrategy.getStrategyName());
        } catch (Exception e) {
            selectedStrategy = strategyFactory.getCacheAsideStrategy();
            log.warn("Fallback to cache-aside strategy due to initialization error", e);
        }
    }

    @Bean
    public CacheStrategy primaryCacheStrategy() {
        return selectedStrategy;
    }
}
