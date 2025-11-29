package com.example.lcaliteservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.lcaliteservice.dto.LcaComputedEvent;

@Component
public class LcaEventProducer {

	public static final String LCA_COMPLETED_TOPIC = "lca.completed";

	private static final Logger log = LoggerFactory.getLogger(LcaEventProducer.class);

	private final KafkaTemplate<String, LcaComputedEvent> kafkaTemplate;

	public LcaEventProducer(KafkaTemplate<String, LcaComputedEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publish(LcaComputedEvent event) {
		log.info("Publishing LCA result {} for product {}", event.getResultId(), event.getProductId());
		kafkaTemplate.send(LCA_COMPLETED_TOPIC, event.getProductId().toString(), event);
	}
}

