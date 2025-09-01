package com.puppytalk.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Redis 설정
 */
@Configuration
public class RedisConfig {
    
    @Bean
    @Profile("!test")
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password) {
        
        Config config = new Config();
        
        String address = "redis://" + host + ":" + port;
        config.useSingleServer()
            .setAddress(address)
            .setConnectionMinimumIdleSize(1)
            .setConnectionPoolSize(5)
            .setDnsMonitoringInterval(5000)
            .setRetryAttempts(3)
            .setRetryInterval(1000)
            .setTimeout(3000)
            .setConnectTimeout(10000)
            .setIdleConnectionTimeout(10000);
        
        if (!password.isBlank()) {
            config.useSingleServer().setPassword(password);
        }
        
        return Redisson.create(config);
    }
    
    @Bean
    @Profile("test")
    public RedissonClient testRedissonClient() {
        // 테스트용 인메모리 Redis 클라이언트
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://localhost:6379")
            .setConnectionMinimumIdleSize(1)
            .setConnectionPoolSize(2);
            
        return Redisson.create(config);
    }
}