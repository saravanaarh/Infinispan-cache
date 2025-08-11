package com.project.cache.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CacheStrategyFactory {

    private final Map<String, CacheStrategy> strategies;

    @Autowired
    public CacheStrategyFactory(Map<String, CacheStrategy> strategies) {
        this.strategies = strategies;
    }

    public CacheStrategy getStrategy(String strategyName) {
        CacheStrategy strategy = strategies.get(strategyName + "Strategy");
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown cache strategy: " + strategyName);
        }
        return strategy;
    }

    //Save to database first, then try to cache (if cache fails, log warning and continue - fault-tolerant).
    public CacheStrategy getCacheAsideStrategy() {return getStrategy("cacheAside"); }

    //Save to database and cache within same transaction - if either fails, both rollback (strict consistency).
    public CacheStrategy getWriteThroughStrategy() {
        return getStrategy("writeThrough");
    }

    //Pre-check cache availability, then save to database and cache within transaction - fail immediately if cache unavailable (proactive validation).
    public CacheStrategy getFailFastStrategy() {
        return getStrategy("failFast");
    }

    //Save to database immediately, then update cache asynchronously in background (non-blocking, eventual consistency).
    public CacheStrategy getAsyncCacheStrategy() {
        return getStrategy("asyncCache");
    }
}
