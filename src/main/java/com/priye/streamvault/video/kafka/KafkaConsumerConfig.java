package com.priye.streamvault.video.kafka;

import com.priye.streamvault.video.service.VideoStatusService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final VideoStatusService videoStatusService;

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {

        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) ->
                        new TopicPartition(
                                record.topic() + ".DLQ",
                                record.partition()
                        )
        );
    }

    @Bean
    public ConsumerRecordRecoverer consumerRecordRecoverer(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {

        return (record, exception) -> {

            // 1. Mark video as FAILED
            markVideoAsFailed(record);

            // 2. Publish failed Kafka message to DLQ
            deadLetterPublishingRecoverer.accept(record, exception);
        };
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            ConsumerRecordRecoverer consumerRecordRecoverer) {

        /*
         * 2 second delay between attempts.
         *
         * 2 retries means:
         *
         * Attempt 1 -> original attempt
         * Attempt 2 -> retry #1
         * Attempt 3 -> retry #2
         *
         * After that -> recoverer -> DLQ
         */
        FixedBackOff backOff = new FixedBackOff(2000L, 2L);

        return new DefaultErrorHandler(consumerRecordRecoverer, backOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VideoEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, VideoEvent> consumerFactory, DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, VideoEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }

    private void markVideoAsFailed(ConsumerRecord<?, ?> record) {

        Object value = record.value();

        if (!(value instanceof VideoEvent videoEvent)) {
            return;
        }

        videoStatusService.markFailed(videoEvent.videoId());
    }
}