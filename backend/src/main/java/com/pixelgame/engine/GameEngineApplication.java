package com.pixelgame.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GameEngineApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GameEngineApplication.class, args);
    }
}

