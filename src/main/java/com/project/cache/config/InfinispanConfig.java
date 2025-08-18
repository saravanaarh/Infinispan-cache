package com.project.cache.config;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;


@Slf4j
public class InfinispanConfig {

    private final String configFile;
    private final String mode;
    private EmbeddedCacheManager cacheManager;
    @Getter
    private Cache<String, Object> cache;

    public InfinispanConfig(String configFile, String mode) {
        this.configFile = configFile;
        this.mode = mode;
        init();
    }

    private void init() {
        try {
            log.info("Initializing Infinispan {} cache", mode);
            cacheManager = new DefaultCacheManager(configFile);
            cache = cacheManager.getCache("studentsCache");
            log.info("Infinispan {} cache initialized successfully", mode);
        } catch (Exception e) {
            log.error("Failed to initialize {} cache", mode, e);
            throw new RuntimeException("Cache initialization failed", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (cacheManager != null) {
            log.info("Shutting down Infinispan {} cache", mode);
            cacheManager.stop();
        }
    }
}
