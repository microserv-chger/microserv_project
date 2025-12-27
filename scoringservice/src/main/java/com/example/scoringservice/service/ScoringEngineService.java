package com.example.scoringservice.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scoringservice.dto.LcaComputedEvent;
import com.example.scoringservice.dto.ScoreComputeRequest;
import com.example.scoringservice.dto.ScorePublishedEvent;
import com.example.scoringservice.dto.ScoreResponse;
import com.example.scoringservice.entity.EcoScore;
import com.example.scoringservice.messaging.ScoreEventProducer;
import com.example.scoringservice.repository.EcoScoreRepository;

@Service
public class ScoringEngineService {

	private final EcoScoreRepository repository;
	private final ScoreEventProducer eventProducer;

	public ScoringEngineService(EcoScoreRepository repository, ScoreEventProducer eventProducer) {
		this.repository = repository;
		this.eventProducer = eventProducer;
	}

	@Transactional
	public ScoreResponse compute(ScoreComputeRequest request) {
		return persistScore(request.getProductId(), request.getCo2Kg(), request.getWaterLiters(),
				request.getEnergyMj());
	}

	@Transactional
	public void processLcaEvent(LcaComputedEvent event) {
		persistScore(event.getProductId(), event.getCo2Kg(), event.getWaterLiters(), event.getEnergyMj());
	}

	private ScoreResponse persistScore(java.util.UUID productId, double co2, double water, double energy) {
		double normalizedValue = normalizeScore(co2, water, energy);
		String letter = toLetter(normalizedValue);
		double confidence = calculateConfidence(co2, water, energy);
		List<String> explanations = buildExplanations(co2, water, energy);

		EcoScore score = repository.findByProductId(productId).orElseGet(EcoScore::new);
		score.setProductId(productId);
		score.setScoreValue(normalizedValue);
		score.setScoreLetter(letter);
		score.setConfidence(confidence);
		score.setCo2(co2);
		score.setWater(water);
		score.setEnergy(energy);
		if (score.getExplanations() == null) {
			score.setExplanations(new java.util.ArrayList<>(explanations));
		} else {
			score.getExplanations().clear();
			score.getExplanations().addAll(explanations);
		}
		score.setCalculatedAt(Instant.now());
		EcoScore saved = repository.save(score);

		eventProducer.publish(new ScorePublishedEvent(productId, saved.getId(), saved.getScoreValue(),
				saved.getScoreLetter(), saved.getConfidence(), saved.getCo2(), saved.getWater(), saved.getEnergy(),
				saved.getExplanations(), saved.getCalculatedAt()));

		return new ScoreResponse(saved.getId(), saved.getProductId(), saved.getScoreValue(), saved.getScoreLetter(),
				saved.getConfidence(), saved.getCo2(), saved.getWater(), saved.getEnergy(),
				saved.getExplanations(), saved.getCalculatedAt());
	}

	private double normalizeScore(double co2, double water, double energy) {
		// Benchmarks basés sur les données observées
		// CO2: 0-10 kg -> 100-0
		// Eau: 0-1000 L -> 100-0
		// Energie: 0-50 MJ -> 100-0

		double co2Score = Math.max(0, 100 * (1 - (co2 / 10.0)));
		double waterScore = Math.max(0, 100 * (1 - (water / 1000.0)));
		double energyScore = Math.max(0, 100 * (1 - (energy / 50.0)));

		// Pondération: 50% CO2, 25% Eau, 25% Energie
		double total = (co2Score * 0.5) + (waterScore * 0.25) + (energyScore * 0.25);
		return Math.round(total * 100.0) / 100.0;
	}

	private String toLetter(double score) {
		if (score >= 80) {
			return "A";
		}
		if (score >= 65) {
			return "B";
		}
		if (score >= 50) {
			return "C";
		}
		if (score >= 35) {
			return "D";
		}
		return "E";
	}

	private double calculateConfidence(double co2, double water, double energy) {
		double variance = (co2 + water + energy) / 300;
		double confidence = 1 - Math.min(variance, 0.6);
		return Math.round(confidence * 100.0) / 100.0;
	}

	private List<String> buildExplanations(double co2, double water, double energy) {
		return new java.util.ArrayList<>(List.of(
				String.format("Empreinte carbone: %.2f kg CO2e", co2),
				String.format("Consommation d'eau: %.2f L", water),
				String.format("Energie cumulée: %.2f MJ", energy),
				"Pondération appliquée: 50% Carbone, 25% Eau, 25% Energie."));
	}
}
