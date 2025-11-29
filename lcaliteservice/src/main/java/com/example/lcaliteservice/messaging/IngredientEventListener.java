package com.example.lcaliteservice.messaging;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.lcaliteservice.dto.IngredientNormalizedEvent;
import com.example.lcaliteservice.dto.LcaCalculationRequest;
import com.example.lcaliteservice.service.LcaCalculatorService;

@Component
public class IngredientEventListener {

	private static final Logger log = LoggerFactory.getLogger(IngredientEventListener.class);

	private final LcaCalculatorService calculatorService;

	public IngredientEventListener(LcaCalculatorService calculatorService) {
		this.calculatorService = calculatorService;
	}

	@KafkaListener(topics = "ingredients.normalized", groupId = "lca-lite-service")
	public void handleIngredients(IngredientNormalizedEvent event) {
		log.info("Received {} normalized ingredients for product {}", event.getIngredients().size(),
				event.getProductId());
		List<LcaCalculationRequest.IngredientImpact> ingredients = event.getIngredients()
				.stream()
				.map(item -> {
					LcaCalculationRequest.IngredientImpact impact = new LcaCalculationRequest.IngredientImpact();
					impact.setName(item.getName());
					impact.setCategory(item.getCategory());
					impact.setImpactHint(item.getImpactHint());
					return impact;
				}).toList();
		calculatorService.processIngredientEvent(event.getProductId(), ingredients);
	}
}

