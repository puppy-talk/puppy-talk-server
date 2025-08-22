package com.puppytalk.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.puppytalk")
@EntityScan(basePackages = "com.puppytalk")
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {

}