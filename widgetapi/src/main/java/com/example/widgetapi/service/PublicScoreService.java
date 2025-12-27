package com.example.widgetapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.widgetapi.dto.PublicScoreResponse;
import com.example.widgetapi.entity.PublicProductScore;
import com.example.widgetapi.repository.PublicProductScoreRepository;

@Service
public class PublicScoreService {

	private static final Logger log = LoggerFactory.getLogger(PublicScoreService.class);

	private final PublicProductScoreRepository repository;
	private final RestTemplate restTemplate;

	public PublicScoreService(PublicProductScoreRepository repository, RestTemplate restTemplate) {
		this.repository = repository;
		this.restTemplate = restTemplate;
	}

	@Transactional(readOnly = true)
	public List<PublicScoreResponse> getAllScores() {
		return repository.findAll().stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Optional<PublicScoreResponse> findScore(UUID productId) {
		return repository.findByProductId(productId)
				.map(this::toResponse)
				.or(() -> fetchFromScoringService(productId));
	}

	@Transactional
	public PublicScoreResponse saveOrUpdate(PublicProductScore score) {
		PublicProductScore saved = repository.findByProductId(score.getProductId())
				.map(existing -> {
					existing.setScoreValue(score.getScoreValue());
					existing.setScoreLetter(score.getScoreLetter());
					existing.setConfidence(score.getConfidence());
					existing.setCo2(score.getCo2());
					existing.setWater(score.getWater());
					existing.setEnergy(score.getEnergy());
					existing.setExplanations(score.getExplanations());
					existing.setCalculatedAt(score.getCalculatedAt());
					return repository.save(existing);
				})
				.orElseGet(() -> repository.save(score));
		return toResponse(saved);
	}

	private Optional<PublicScoreResponse> fetchFromScoringService(UUID productId) {
		try {
			ResponseEntity<PublicScoreResponse> response = restTemplate
					.getForEntity("http://scoring-service/score/product/{productId}",
							PublicScoreResponse.class, productId);
			return Optional.ofNullable(response.getBody());
		} catch (Exception ex) {
			log.warn("Unable to fetch score from scoring-service via Eureka: {}", ex.getMessage());
			return Optional.empty();
		}
	}

	private PublicScoreResponse toResponse(PublicProductScore score) {
		// Détacher explicitement la collection Hibernate pour éviter
		// LazyInitializationException hors session
		List<String> explanations = (score.getExplanations() != null)
				? new ArrayList<>(score.getExplanations())
				: new ArrayList<>();

		return new PublicScoreResponse(
				score.getProductId(),
				score.getId(),
				score.getScoreValue(),
				score.getScoreLetter() != null ? score.getScoreLetter() : "N/A",
				score.getConfidence(),
				score.getCo2() != null ? score.getCo2() : 0.0,
				score.getWater() != null ? score.getWater() : 0.0,
				score.getEnergy() != null ? score.getEnergy() : 0.0,
				explanations,
				score.getCalculatedAt());
	}
}
