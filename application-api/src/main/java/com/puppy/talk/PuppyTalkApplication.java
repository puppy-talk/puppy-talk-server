package com.puppy.talk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.puppy.talk")
@EnableScheduling
public class PuppyTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(PuppyTalkApplication.class, args);
    }
}