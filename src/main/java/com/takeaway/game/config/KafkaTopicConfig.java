package com.takeaway.game.config;

import com.takeaway.game.kafka.KafkaService;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value(value = "${kafka.topic.ready-for-game}")
    private String READY_FOR_GAME_TOPIC;

    @Value(value = "${kafka.topic.invite-to-a-game}")
    private String INVITE_TO_GAME_TOPIC;

    @Value(value = "${kafka.topic.move}")
    private String MOVE_GAME_TOPIC;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress
        ));
    }

    @Bean
    public NewTopic readyTopic() {
        return TopicBuilder
                .name(READY_FOR_GAME_TOPIC)
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "180000")
                .build();
    }

    @Bean
    public NewTopic inviteTopic() {
        return TopicBuilder
                .name(INVITE_TO_GAME_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic gameMoveTopic() {
        return TopicBuilder
                .name(MOVE_GAME_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
