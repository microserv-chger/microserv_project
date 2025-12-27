package com.example.widgetapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.widgetapi.dto.ScorePublishedEvent;
import com.example.widgetapi.entity.PublicProductScore;
import com.example.widgetapi.service.PublicScoreService;

@Component
public class ScoreEventListener {

	private static final Logger log = LoggerFactory.getLogger(ScoreEventListener.class);

	private final PublicScoreService scoreService;

	public ScoreEventListener(PublicScoreService scoreService) {
		this.scoreService = scoreService;
	}

	@KafkaListener(topics = "score.published", groupId = "widget-api")
	public void handleScorePublished(ScorePublishedEvent event) {
		log.info("Updating public catalog with score {} for product {}", event.getScoreId(),
				event.getProductId());
		PublicProductScore score = new PublicProductScore();
		score.setProductId(event.getProductId());
		score.setScoreValue(event.getScoreValue());
		score.setScoreLetter(event.getScoreLetter());
		score.setConfidence(event.getConfidence());
		score.setCo2(event.getCo2());
		score.setWater(event.getWater());
		score.setEnergy(event.getEnergy());
		score.setExplanations(event.getExplanations());
		score.setCalculatedAt(event.getCalculatedAt());
		scoreService.saveOrUpdate(score);
	}
}
