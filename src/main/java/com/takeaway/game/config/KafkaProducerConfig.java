package com.takeaway.game.config;

import com.takeaway.game.kafka.dto.Announcement;
import com.takeaway.game.kafka.dto.Invite;
import com.takeaway.game.kafka.dto.RemoteMove;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value(value = "${kafka.topic.ready-for-game}")
    private String READY_FOR_GAME_TOPIC;

    @Value(value = "${kafka.topic.invite-to-a-game}")
    private String INVITE_TO_GAME_TOPIC;

    @Value(value = "${kafka.topic.move}")
    private String MOVE_GAME_TOPIC;

    @Bean
    public ProducerFactory producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        configProps.put(
                JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Announcement> anouncementTemplate() {
        KafkaTemplate<String, Announcement> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(READY_FOR_GAME_TOPIC);
        return template;
    }

    @Bean
    public KafkaTemplate<String, Invite> inviteKafkaTemplate() {
        KafkaTemplate<String, Invite> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(INVITE_TO_GAME_TOPIC);
        return template;
    }

    @Bean
    public KafkaTemplate<String, RemoteMove> moveKafkaTemplate() {
        KafkaTemplate<String, RemoteMove> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(MOVE_GAME_TOPIC);
        return template;
    }
}