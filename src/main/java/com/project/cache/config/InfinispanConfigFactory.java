package com.project.cache.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class InfinispanConfigFactory {
    @Bean
    @Profile("local")
    public InfinispanConfig localCacheProvider() {
        return new InfinispanConfig("infinispan-local.xml", "LOCAL");
    }

    @Bean
    @Profile("cluster")
    public InfinispanConfig clusterCacheProvider() {
        return new InfinispanConfig("infinispan-cluster.xml", "CLUSTER");
    }
}
