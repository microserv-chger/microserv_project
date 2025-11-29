package com.example.nlpingredientservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.nlpingredientservice.dto.ProductParsedEvent;
import com.example.nlpingredientservice.service.NlpExtractionService;

@Component
public class ProductParsedListener {

	private static final Logger log = LoggerFactory.getLogger(ProductParsedListener.class);

	private final NlpExtractionService extractionService;

	public ProductParsedListener(NlpExtractionService extractionService) {
		this.extractionService = extractionService;
	}

	@KafkaListener(topics = "product.parsed", groupId = "nlp-ingredient-service")
	public void handleProductParsed(ProductParsedEvent event) {
		log.info("Consuming parsed product {} for NLP extraction", event.getProductId());
		extractionService.processProductEvent(event.getProductId(), event.getNormalizedText());
	}
}

