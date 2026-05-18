package com.example.authstarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author 𝓟𝓔𝓣𝓔𝓡
 * */
@SpringBootApplication
@EnableJpaAuditing
public class AuthStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthStarterApplication.class, args);
    }

}
