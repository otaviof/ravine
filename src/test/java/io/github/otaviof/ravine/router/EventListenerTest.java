package io.github.otaviof.ravine.router;

import static org.awaitility.Awaitility.await;

import io.github.otaviof.ravine.config.Config;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Config.class, EventListener.class})
@Slf4j
public class EventListenerTest implements ApplicationEventPublisherAware {
    @Autowired
    private EventListener listener;

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @BeforeEach
    void prepare() {
        publisher.publishEvent(new Event(this, "test", null));
    }

    @Test
    void inCache() {
        await().atMost(200, TimeUnit.MILLISECONDS)
                .until(() -> listener.inCache("test"));
    }

    @Test
    void getEvent() {
        var event = listener.getEvent("test");

        Assertions.assertThat(event).isNotNull();
        Assertions.assertThat("test").isEqualTo(event.getK());
    }
}