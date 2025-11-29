package com.example.nlpingredientservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.example.nlpingredientservice.messaging.IngredientEventProducer;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic ingredientTopic() {
		return TopicBuilder.name(IngredientEventProducer.INGREDIENTS_NORMALIZED_TOPIC)
				.partitions(3)
				.replicas(1)
				.build();
	}
}

