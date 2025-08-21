package com.puppytalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.puppytalk")
public class PuppyTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(PuppyTalkApplication.class, args);
    }

}