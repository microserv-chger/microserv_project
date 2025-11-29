package com.example.parserproduitservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.example.parserproduitservice.messaging.ProductEventProducer;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic productParsedTopic() {
		return TopicBuilder.name(ProductEventProducer.PRODUCT_PARSED_TOPIC)
				.partitions(3)
				.replicas(1)
				.compact()
				.build();
	}
}

