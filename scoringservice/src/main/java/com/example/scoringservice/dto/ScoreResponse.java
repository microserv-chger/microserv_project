package com.example.scoringservice.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ScoreResponse {

	private UUID scoreId;
	private UUID productId;
	private double scoreValue;
	private String scoreLetter;
	private double confidence;
	private double co2;
	private double water;
	private double energy;
	private List<String> explanations;
	private Instant calculatedAt;

	public ScoreResponse(UUID scoreId, UUID productId, double scoreValue, String scoreLetter,
			double confidence, double co2, double water, double energy,
			List<String> explanations, Instant calculatedAt) {
		this.scoreId = scoreId;
		this.productId = productId;
		this.scoreValue = scoreValue;
		this.scoreLetter = scoreLetter;
		this.confidence = confidence;
		this.co2 = co2;
		this.water = water;
		this.energy = energy;
		this.explanations = explanations;
		this.calculatedAt = calculatedAt;
	}

	public UUID getScoreId() {
		return scoreId;
	}

	public UUID getProductId() {
		return productId;
	}

	public double getScoreValue() {
		return scoreValue;
	}

	public String getScoreLetter() {
		return scoreLetter;
	}

	public double getConfidence() {
		return confidence;
	}

	public double getCo2() {
		return co2;
	}

	public double getWater() {
		return water;
	}

	public double getEnergy() {
		return energy;
	}

	public List<String> getExplanations() {
		return explanations;
	}

	public Instant getCalculatedAt() {
		return calculatedAt;
	}
}
