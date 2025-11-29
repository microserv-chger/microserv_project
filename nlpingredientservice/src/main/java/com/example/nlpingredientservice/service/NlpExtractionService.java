package com.example.nlpingredientservice.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nlpingredientservice.dto.IngredientNormalizedEvent;
import com.example.nlpingredientservice.dto.NlpExtractRequest;
import com.example.nlpingredientservice.dto.IngredientNormalizedEvent.NormalizedIngredientPayload;
import com.example.nlpingredientservice.entity.NormalizedIngredient;
import com.example.nlpingredientservice.messaging.IngredientEventProducer;
import com.example.nlpingredientservice.repository.NormalizedIngredientRepository;

@Service
public class NlpExtractionService {

	private static final Pattern ORGANIC_PATTERN = Pattern.compile("\\b(bio|organic)\\b", Pattern.CASE_INSENSITIVE);

	private final NormalizedIngredientRepository repository;
	private final IngredientEventProducer eventProducer;

	public NlpExtractionService(NormalizedIngredientRepository repository,
			IngredientEventProducer eventProducer) {
		this.repository = repository;
		this.eventProducer = eventProducer;
	}

	@Transactional
	public List<NormalizedIngredient> processText(NlpExtractRequest request) {
		return process(request.getProductId(), request.getText());
	}

	@Transactional
	public List<NormalizedIngredient> processProductEvent(UUID productId, String normalizedText) {
		return process(productId, normalizedText);
	}

	private List<NormalizedIngredient> process(UUID productId, String text) {
		repository.findByProductId(productId).forEach(existing -> repository.deleteById(existing.getId()));

		String sanitized = text == null ? "" : text.toLowerCase(Locale.ROOT);
		String[] tokens = sanitized.split("[,;\\n]");

		List<NormalizedIngredient> saved = new ArrayList<>();
		for (String token : tokens) {
			String ingredientName = token.trim();
			if (ingredientName.isEmpty()) {
				continue;
			}
			NormalizedIngredient ingredient = new NormalizedIngredient();
			ingredient.setProductId(productId);
			ingredient.setName(capitalize(ingredientName.replaceAll("\\d+%", "").trim()));
			ingredient.setCategory(classifyIngredient(ingredientName));
			ingredient.setEcoReference("EcoInvent-v1");
			ingredient.setOrganic(isOrganic(token));
			ingredient.setImpactHint(estimateImpact(ingredient.getCategory()));
			ingredient.setExtractedAt(Instant.now());
			saved.add(repository.save(ingredient));
		}

		List<NormalizedIngredientPayload> payload = saved.stream()
				.map(i -> new NormalizedIngredientPayload(i.getName(), i.getCategory(), i.getEcoReference(),
						i.isOrganic(), i.getImpactHint()))
				.toList();
		eventProducer.publish(new IngredientNormalizedEvent(productId, payload));
		return saved;
	}

	private boolean isOrganic(String token) {
		Matcher matcher = ORGANIC_PATTERN.matcher(token);
		return matcher.find();
	}

	private String classifyIngredient(String name) {
		if (name.contains("milk") || name.contains("lait")) {
			return "DAIRY";
		}
		if (name.contains("sugar") || name.contains("sucre")) {
			return "SWEETENER";
		}
		if (name.contains("plastic") || name.contains("pet")) {
			return "PACKAGING";
		}
		if (name.contains("glass")) {
			return "GLASS";
		}
		return "OTHER";
	}

	private double estimateImpact(String category) {
		return switch (category) {
			case "DAIRY" -> 2.5;
			case "SWEETENER" -> 1.3;
			case "PACKAGING" -> 3.2;
			case "GLASS" -> 1.1;
			default -> 0.9;
		};
	}

	private String capitalize(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}

