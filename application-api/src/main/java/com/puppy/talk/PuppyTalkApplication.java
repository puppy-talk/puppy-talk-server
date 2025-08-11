package com.puppy.talk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.puppy.talk")
public class PuppyTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(PuppyTalkApplication.class, args);
    }
}