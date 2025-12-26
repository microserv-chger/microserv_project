package com.example.nlpingredientservice.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nlpingredientservice.client.MlServiceClient;
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
	private final MlServiceClient mlServiceClient;

	public NlpExtractionService(NormalizedIngredientRepository repository,
			IngredientEventProducer eventProducer, MlServiceClient mlServiceClient) {
		this.repository = repository;
		this.eventProducer = eventProducer;
		this.mlServiceClient = mlServiceClient;
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

		// Essayer d'abord l'extraction ML (spaCy + BERT)
		MlServiceClient.MlIngredientResponse mlResponse = mlServiceClient.extractIngredients(text);
		List<NormalizedIngredient> saved;

		if (mlResponse != null && mlResponse.getIngredients() != null && !mlResponse.getIngredients().isEmpty()) {
			// Utiliser les résultats du ML
			saved = processMlResults(productId, mlResponse);
		} else {
			// Fallback: parsing basique avec regex
			saved = processFallback(productId, text);
		}

		List<NormalizedIngredientPayload> payload = saved.stream()
				.map(i -> new NormalizedIngredientPayload(i.getName(), i.getCategory(), i.getEcoReference(),
						i.isOrganic(), i.getImpactHint()))
				.toList();
		eventProducer.publish(new IngredientNormalizedEvent(productId, payload));
		return saved;
	}

	/**
	 * Traite les résultats du service ML (spaCy + BERT)
	 */
	private List<NormalizedIngredient> processMlResults(UUID productId,
			MlServiceClient.MlIngredientResponse mlResponse) {
		List<NormalizedIngredient> saved = new ArrayList<>();
		boolean isOrganic = mlResponse.isOrganic();

		for (Map<String, Object> ingMap : mlResponse.getIngredients()) {
			String name = (String) ingMap.get("name");
			String category = (String) ingMap.getOrDefault("category", "OTHER");
			Double confidence = ingMap.get("confidence") != null
					? ((Number) ingMap.get("confidence")).doubleValue()
					: 0.5;

			if (name == null || name.trim().isEmpty()) {
				continue;
			}

			NormalizedIngredient ingredient = new NormalizedIngredient();
			ingredient.setProductId(productId);
			ingredient.setName(capitalize(name.trim()));
			ingredient.setCategory(category);
			ingredient.setEcoReference("EcoInvent-v1");
			ingredient.setOrganic(isOrganic || confidence > 0.7); // Considérer bio si haute confiance
			// Note: impactHint n'est PAS défini ici - c'est le rôle du service AVC
			ingredient.setExtractedAt(Instant.now());
			saved.add(repository.save(ingredient));
		}

		return saved;
	}

	/**
	 * Fallback: parsing basique avec regex (méthode originale)
	 */
	private List<NormalizedIngredient> processFallback(UUID productId, String text) {
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
			// Note: impactHint n'est PAS défini ici - c'est le rôle du service AVC
			ingredient.setExtractedAt(Instant.now());
			saved.add(repository.save(ingredient));
		}
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
