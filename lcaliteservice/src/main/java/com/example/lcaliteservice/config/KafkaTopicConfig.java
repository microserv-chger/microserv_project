package com.example.lcaliteservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.example.lcaliteservice.messaging.LcaEventProducer;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic lcaCompletedTopic() {
		return TopicBuilder.name(LcaEventProducer.LCA_COMPLETED_TOPIC)
				.partitions(3)
				.replicas(1)
				.build();
	}
}

