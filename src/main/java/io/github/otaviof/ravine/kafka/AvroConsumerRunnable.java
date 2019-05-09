package io.github.otaviof.ravine.kafka;

import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper to create a background thread with Kafka Streams consumer.
 */
@Slf4j
public class AvroConsumerRunnable implements Runnable {
    private final AvroConsumer consumer;

    public AvroConsumerRunnable(AvroConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Start Kafka Streams thread.
     */
    @Override
    public void run() {
        log.info("Starting consumer...");
        consumer.getStreams().start();
    }
}
