package io.github.otaviof.ravine.router;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listen to events and store them in a simple cache mechanism.
 */
@Component
@Slf4j
public class EventListener implements ApplicationListener<Event> {
    private final Map<String, Event> cache;

    public EventListener() {
        this.cache = new HashMap<>();
    }

    /**
     * Check if a given key is in listener's cache.
     *
     * @param key string key name;
     * @return boolean;
     */
    boolean inCache(String key) {
        return cache.keySet().contains(key);
    }

    /**
     * Retrieve an event from cache.
     *
     * @param key string key name;
     * @return Event object;
     */
    Event getEvent(String key) {
        return cache.get(key);
    }

    /**
     * Given timeout, remove from cache events that are older than that.
     *
     * @param timeoutMs timeout in milliseconds;
     */
    void expireOlderThan(int timeoutMs) {
        var expiredAt = new Date(new Date().getTime() - timeoutMs);
        var toDelete = cache.entrySet().stream()
                        .filter(c -> c.getValue().getCreatedAt().before(expiredAt))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(HashSet::new));

        log.info("Expiring '{}' entries in cache...", toDelete.size());
        toDelete.forEach(cache::remove);
    }

    /**
     * Store received events in cache.
     *
     * @param event Event object;
     */
    @Override
    public void onApplicationEvent(Event event) {
        log.info("Received application event key: '{}'", event.getK());
        if (inCache(event.getK())) {
            log.debug("Event key '{}' is already in cache", event.getK());
            return;
        }
        cache.put(event.getK(), event);
    }
}
