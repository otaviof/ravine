package io.github.otaviof.ravine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ravine Application.
 */
@SpringBootApplication
@Slf4j
public class Ravine  {
    public static void main(String[] args) {
        SpringApplication.run(Ravine.class, args);
    }
}
