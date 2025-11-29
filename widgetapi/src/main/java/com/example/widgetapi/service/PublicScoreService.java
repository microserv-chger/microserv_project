package com.example.widgetapi.service;

import java.util.Optional;
import java.util.UUID;

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
		}
		catch (Exception ex) {
			log.warn("Unable to fetch score from scoring-service via Eureka: {}", ex.getMessage());
			return Optional.empty();
		}
	}

	private PublicScoreResponse toResponse(PublicProductScore score) {
		return new PublicScoreResponse(score.getProductId(), score.getId(), score.getScoreValue(),
				score.getScoreLetter(), score.getConfidence(), score.getExplanations(), score.getCalculatedAt());
	}
}

