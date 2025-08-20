package com.project.cache.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class InfinispanConfigFactory {

    @Value("${infinispan.cache.type:distributed}") // distributed or replicated
    private String cacheType;

    @Bean
    @Profile("local")
    public InfinispanConfig localCacheProvider() {
        return new InfinispanConfig("infinispan-local.xml", "LOCAL");
    }

    @Bean
    @Profile("cluster")
    public InfinispanConfig clusterCacheProvider() {
        String configFile = "distributed".equals(cacheType)
                ? "infinispan-distributed.xml"
                : "infinispan-replicated.xml";
        return new InfinispanConfig(configFile, "CLUSTER");
    }
}
