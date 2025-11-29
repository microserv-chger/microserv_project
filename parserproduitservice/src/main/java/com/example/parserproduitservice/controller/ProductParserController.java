package com.example.parserproduitservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.parserproduitservice.dto.ProductParseRequest;
import com.example.parserproduitservice.dto.ProductParseResponse;
import com.example.parserproduitservice.entity.ProductMetadata;
import com.example.parserproduitservice.repository.ProductMetadataRepository;
import com.example.parserproduitservice.service.ProductParserService;

@RestController
@RequestMapping("/product")
public class ProductParserController {

	private final ProductParserService parserService;
	private final ProductMetadataRepository repository;

	public ProductParserController(ProductParserService parserService, ProductMetadataRepository repository) {
		this.parserService = parserService;
		this.repository = repository;
	}

	@PostMapping("/parse")
	public ResponseEntity<ProductParseResponse> parseProduct(@Validated @RequestBody ProductParseRequest request) {
		ProductMetadata saved = parserService.parse(request);
		return ResponseEntity.ok(new ProductParseResponse(saved.getId(), "PARSED"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductMetadata> getParsedProduct(@PathVariable UUID id) {
		return repository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}

