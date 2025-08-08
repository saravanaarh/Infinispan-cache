package com.project.cache.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class InfinispanConfig {

    private EmbeddedCacheManager cacheManager;

    @Getter
    private Cache<String, Object> cache;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Infinispan cache");

            cacheManager = new DefaultCacheManager("infinispan.xml");

            cache = cacheManager.getCache("studentsCache");

            log.info("Infinispan cache initialized successfully from XML");

        } catch (IOException e) {
            log.error("Failed to load infinispan.xml configuration", e);
            throw new RuntimeException("Cache initialization failed", e);
        } catch (Exception e) {
            log.error("Failed to initialize Infinispan cache", e);
            throw new RuntimeException("Cache initialization failed", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (cacheManager != null) {
            log.info("Shutting down Infinispan cache");
            cacheManager.stop();
        }
    }
}
