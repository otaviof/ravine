package io.github.otaviof.ravine.router;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Listen to events and store them in a simple cache mechanism.
 */
@Component
@Slf4j
public class SpecificEventListener implements ApplicationListener<Event> {
    private Map<String, Event> cache;

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
        toDelete.forEach(c -> cache.remove(c));
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
     * Store received events in cache.
     *
     * @param event Event object;
     */
    @Override
    public void onApplicationEvent(Event event) {
        log.info("Received application event key: '{}'", event.getK());
        cache.put(event.getK(), event);
    }

    public SpecificEventListener() {
        this.cache = new HashMap<>();
    }
}
