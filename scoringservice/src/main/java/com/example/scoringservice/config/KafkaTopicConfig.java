package com.example.scoringservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.example.scoringservice.messaging.ScoreEventProducer;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic scorePublishedTopic() {
		return TopicBuilder.name(ScoreEventProducer.SCORE_PUBLISHED_TOPIC)
				.partitions(3)
				.replicas(1)
				.build();
	}
}

