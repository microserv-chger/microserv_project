package com.example.nlpingredientservice.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nlpingredientservice.dto.NlpExtractRequest;
import com.example.nlpingredientservice.dto.ProductWithIngredients;
import com.example.nlpingredientservice.dto.ProductWithIngredients.IngredientDto;
import com.example.nlpingredientservice.entity.NormalizedIngredient;
import com.example.nlpingredientservice.repository.NormalizedIngredientRepository;
import com.example.nlpingredientservice.service.NlpExtractionService;

@RestController
@RequestMapping("/nlp")
public class NlpExtractionController {

	private final NlpExtractionService extractionService;
	private final NormalizedIngredientRepository repository;

	public NlpExtractionController(NlpExtractionService extractionService,
			NormalizedIngredientRepository repository) {
		this.extractionService = extractionService;
		this.repository = repository;
	}

	@PostMapping("/extract")
	public ResponseEntity<List<NormalizedIngredient>> extract(@Validated @RequestBody NlpExtractRequest request) {
		return ResponseEntity.ok(extractionService.processText(request));
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<List<NormalizedIngredient>> getByProduct(@PathVariable UUID productId) {
		List<NormalizedIngredient> ingredients = repository.findByProductId(productId);
		return ingredients.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(ingredients);
	}

	/**
	 * Get all processed products with their extracted ingredients
	 */
	@GetMapping("/products")
	public ResponseEntity<List<ProductWithIngredients>> getAllProcessedProducts() {
		// Get all ingredients and group by productId
		List<NormalizedIngredient> allIngredients = repository.findAll();

		Map<UUID, List<NormalizedIngredient>> byProduct = allIngredients.stream()
				.collect(Collectors.groupingBy(NormalizedIngredient::getProductId));

		List<ProductWithIngredients> products = byProduct.entrySet().stream()
				.map(entry -> {
					UUID productId = entry.getKey();
					List<NormalizedIngredient> ingredients = entry.getValue();

					List<IngredientDto> ingredientDtos = ingredients.stream()
							.map(i -> new IngredientDto(
									i.getId(),
									i.getName(),
									i.getCategory(),
									i.isOrganic(),
									i.getImpactHint(),
									i.getEcoReference(),
									i.getExtractedAt()))
							.toList();

					// Get the most recent extraction time as processedAt
					var latestExtraction = ingredients.stream()
							.map(NormalizedIngredient::getExtractedAt)
							.max(java.time.Instant::compareTo)
							.orElse(null);

					return new ProductWithIngredients(
							productId,
							null, // GTIN not stored in NormalizedIngredient
							null, // Product name not stored in NormalizedIngredient
							latestExtraction,
							ingredientDtos);
				})
				.sorted((a, b) -> {
					if (a.getProcessedAt() == null)
						return 1;
					if (b.getProcessedAt() == null)
						return -1;
					return b.getProcessedAt().compareTo(a.getProcessedAt());
				})
				.toList();

		return ResponseEntity.ok(products);
	}
}
