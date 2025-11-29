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
	private List<String> explanations;
	private Instant calculatedAt;

	public ScoreResponse(UUID scoreId, UUID productId, double scoreValue, String scoreLetter,
			double confidence, List<String> explanations, Instant calculatedAt) {
		this.scoreId = scoreId;
		this.productId = productId;
		this.scoreValue = scoreValue;
		this.scoreLetter = scoreLetter;
		this.confidence = confidence;
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

	public List<String> getExplanations() {
		return explanations;
	}

	public Instant getCalculatedAt() {
		return calculatedAt;
	}
}

