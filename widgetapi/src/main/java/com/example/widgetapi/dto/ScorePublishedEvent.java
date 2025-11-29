package com.example.widgetapi.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ScorePublishedEvent {

	private UUID productId;
	private UUID scoreId;
	private double scoreValue;
	private String scoreLetter;
	private double confidence;
	private List<String> explanations;
	private Instant calculatedAt;

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public UUID getScoreId() {
		return scoreId;
	}

	public void setScoreId(UUID scoreId) {
		this.scoreId = scoreId;
	}

	public double getScoreValue() {
		return scoreValue;
	}

	public void setScoreValue(double scoreValue) {
		this.scoreValue = scoreValue;
	}

	public String getScoreLetter() {
		return scoreLetter;
	}

	public void setScoreLetter(String scoreLetter) {
		this.scoreLetter = scoreLetter;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public List<String> getExplanations() {
		return explanations;
	}

	public void setExplanations(List<String> explanations) {
		this.explanations = explanations;
	}

	public Instant getCalculatedAt() {
		return calculatedAt;
	}

	public void setCalculatedAt(Instant calculatedAt) {
		this.calculatedAt = calculatedAt;
	}
}

