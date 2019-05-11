package io.github.otaviof.ravine.router;

import static org.awaitility.Awaitility.await;

import io.github.otaviof.ravine.config.Config;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
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

    @Before
    public void prepare() {
        publisher.publishEvent(new Event(this, "test", null));
    }

    @Test
    public void inCache() {
        await().atMost(200, TimeUnit.MILLISECONDS)
                .until(() -> listener.inCache("test"));
    }

    @Test
    public void getEvent() {
        var event = listener.getEvent("test");

        Assert.assertNotNull(event);
        Assert.assertEquals("test", event.getK());
    }
}