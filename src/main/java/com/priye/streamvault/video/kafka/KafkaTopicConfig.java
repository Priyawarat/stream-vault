package com.priye.streamvault.video.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic videoUploadedTopic() {
        return new NewTopic("video-uploaded", 1, (short) 1);
    }

    @Bean
    public NewTopic videoUploadedDlqTopic() {
        return new NewTopic("video-uploaded.DLQ", 1, (short) 1);
    }
}