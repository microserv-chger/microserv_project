package com.example.parserproduitservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.parserproduitservice.dto.ProductParsedEvent;

@Component
public class ProductEventProducer {

	public static final String PRODUCT_PARSED_TOPIC = "product.parsed";

	private static final Logger log = LoggerFactory.getLogger(ProductEventProducer.class);

	private final KafkaTemplate<String, ProductParsedEvent> kafkaTemplate;

	public ProductEventProducer(KafkaTemplate<String, ProductParsedEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publish(ProductParsedEvent event) {
		log.info("Publishing parsed product {} to topic {}", event.getProductId(), PRODUCT_PARSED_TOPIC);
		kafkaTemplate.send(PRODUCT_PARSED_TOPIC, event.getProductId().toString(), event);
	}
}

