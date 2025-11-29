package com.example.nlpingredientservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nlpingredientservice.dto.NlpExtractRequest;
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
}

