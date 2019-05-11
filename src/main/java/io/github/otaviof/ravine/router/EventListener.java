package io.github.otaviof.ravine.router;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.otaviof.ravine.config.Config;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listen to events and store them in a cache with time to expire and maximum amount of items.
 */
@Component
@Slf4j
public class EventListener implements ApplicationListener<Event> {
    private final Cache<String, Event> cache;

    public EventListener(Config config) {
        log.info("Cached event-listener, expire after '{}' ms and maximum entries '{}'",
                config.getCache().getExpireMs(), config.getCache().getMaximumSize());
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(config.getCache().getExpireMs(), TimeUnit.MILLISECONDS)
                .maximumSize(config.getCache().getMaximumSize())
                .build();
    }

    /**
     * Check if a given key is in listener's cache.
     *
     * @param key string key name;
     * @return boolean;
     */
    public boolean inCache(String key) {
        return cache.getIfPresent(key) != null;
    }

    /**
     * Retrieve an event from cache.
     *
     * @param key string key name;
     * @return Event object;
     */
    Event getEvent(String key) {
        return cache.getIfPresent(key);
    }

    /**
     * Store received events in cache.
     *
     * @param event Event object;
     */
    @Override
    public void onApplicationEvent(Event event) {
        log.info("Received application event with key '{}', items in cache '{}'",
                event.getK(), cache.estimatedSize());

        if (inCache(event.getK())) {
            log.warn("Event key '{}' is already in cache, skipping!", event.getK());
            return;
        }

        cache.put(event.getK(), event);
    }
}
