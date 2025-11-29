package com.example.scoringservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.scoringservice.dto.LcaComputedEvent;
import com.example.scoringservice.service.ScoringEngineService;

@Component
public class LcaEventListener {

	private static final Logger log = LoggerFactory.getLogger(LcaEventListener.class);

	private final ScoringEngineService scoringEngineService;

	public LcaEventListener(ScoringEngineService scoringEngineService) {
		this.scoringEngineService = scoringEngineService;
	}

	@KafkaListener(topics = "lca.completed", groupId = "scoring-service")
	public void handleLcaEvent(LcaComputedEvent event) {
		log.info("Scoring product {} from LCA result {}", event.getProductId(), event.getResultId());
		scoringEngineService.processLcaEvent(event);
	}
}

