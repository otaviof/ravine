package io.github.otaviof.ravine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Ravine  {
    public static void main(String[] args) {
        var app = new SpringApplication(Ravine.class);
        app.run(args);
    }
}
