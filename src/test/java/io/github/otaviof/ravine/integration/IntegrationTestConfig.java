package io.github.otaviof.ravine.integration;

import io.github.otaviof.ravine.Ravine;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Set up Spring Boot testing in terms of profile and being able to find Ravine application.
 */
@Profile("test")
@Configuration
@EnableAutoConfiguration
@Import(Ravine.class)
@ComponentScan
public class IntegrationTestConfig {
}
