package com.example.scoringservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scoringservice.dto.ScoreComputeRequest;
import com.example.scoringservice.dto.ScoreResponse;
import com.example.scoringservice.entity.ProvenanceEntry;
import com.example.scoringservice.repository.EcoScoreRepository;
import com.example.scoringservice.repository.ProvenanceEntryRepository;
import com.example.scoringservice.service.ScoringEngineService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/score")
public class ScoreController {

	private final ScoringEngineService scoringEngineService;
	private final EcoScoreRepository repository;
	private final ProvenanceEntryRepository provenanceRepository;

	public ScoreController(ScoringEngineService scoringEngineService, EcoScoreRepository repository,
			ProvenanceEntryRepository provenanceRepository) {
		this.scoringEngineService = scoringEngineService;
		this.repository = repository;
		this.provenanceRepository = provenanceRepository;
	}

	@PostMapping("/compute")
	public ResponseEntity<ScoreResponse> compute(@Validated @RequestBody ScoreComputeRequest request) {
		return ResponseEntity.ok(scoringEngineService.compute(request));
	}

	@Transactional(readOnly = true)
	@GetMapping("/product/{productId}")
	public ResponseEntity<ScoreResponse> getByProduct(@PathVariable UUID productId) {
		return repository.findByProductId(productId)
				.map(score -> new ScoreResponse(score.getId(), score.getProductId(), score.getScoreValue(),
						score.getScoreLetter(), score.getConfidence(), score.getCo2(), score.getWater(),
						score.getEnergy(),
						new java.util.ArrayList<>(score.getExplanations()), score.getCalculatedAt()))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Transactional(readOnly = true)
	@GetMapping("/provenance/{productId}")
	public ResponseEntity<java.util.List<ProvenanceEntry>> getProvenance(@PathVariable UUID productId) {
		return ResponseEntity.ok(provenanceRepository.findByProductIdOrderByTimestampAsc(productId));
	}
}
