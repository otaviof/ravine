package io.github.otaviof.ravine.boot;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.InMemoryReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates a bean for a Jaeger based tracer.
 */
@ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "false")
@Configuration
@Slf4j
public class JaegerTracing {
    @Value("${spring.application.name:ravine}")
    String appName;

    @Bean
    public io.opentracing.Tracer jaegerTracer() {
        log.info("Tracing with Jaeger! '{}'", appName);

        var reporter = new InMemoryReporter();
        var sampler = new ConstSampler(false);

        return new JaegerTracer.Builder(appName)
                .withReporter(reporter)
                .withSampler(sampler)
                .build();
    }
}
