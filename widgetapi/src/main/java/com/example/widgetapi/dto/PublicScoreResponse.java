package com.example.widgetapi.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PublicScoreResponse {

	private UUID productId;
	private UUID scoreId;
	private double scoreValue;
	private String scoreLetter;
	private double confidence;
	private List<String> explanations;
	private Instant calculatedAt;

	public PublicScoreResponse(UUID productId, UUID scoreId, double scoreValue, String scoreLetter,
			double confidence, List<String> explanations, Instant calculatedAt) {
		this.productId = productId;
		this.scoreId = scoreId;
		this.scoreValue = scoreValue;
		this.scoreLetter = scoreLetter;
		this.confidence = confidence;
		this.explanations = explanations;
		this.calculatedAt = calculatedAt;
	}

	public UUID getProductId() {
		return productId;
	}

	public UUID getScoreId() {
		return scoreId;
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

