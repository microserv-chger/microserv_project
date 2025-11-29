package com.example.nlpingredientservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.nlpingredientservice.dto.IngredientNormalizedEvent;

@Component
public class IngredientEventProducer {

	public static final String INGREDIENTS_NORMALIZED_TOPIC = "ingredients.normalized";

	private static final Logger log = LoggerFactory.getLogger(IngredientEventProducer.class);

	private final KafkaTemplate<String, IngredientNormalizedEvent> kafkaTemplate;

	public IngredientEventProducer(KafkaTemplate<String, IngredientNormalizedEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publish(IngredientNormalizedEvent event) {
		log.info("Publishing {} normalized ingredients for product {}", event.getIngredients().size(),
				event.getProductId());
		kafkaTemplate.send(INGREDIENTS_NORMALIZED_TOPIC, event.getProductId().toString(), event);
	}
}

