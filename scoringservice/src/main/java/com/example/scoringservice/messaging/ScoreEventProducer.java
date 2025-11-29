package com.example.scoringservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.scoringservice.dto.ScorePublishedEvent;

@Component
public class ScoreEventProducer {

	public static final String SCORE_PUBLISHED_TOPIC = "score.published";

	private static final Logger log = LoggerFactory.getLogger(ScoreEventProducer.class);

	private final KafkaTemplate<String, ScorePublishedEvent> kafkaTemplate;

	public ScoreEventProducer(KafkaTemplate<String, ScorePublishedEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publish(ScorePublishedEvent event) {
		log.info("Publishing eco-score {} for product {}", event.getScoreId(), event.getProductId());
		kafkaTemplate.send(SCORE_PUBLISHED_TOPIC, event.getProductId().toString(), event);
	}
}

