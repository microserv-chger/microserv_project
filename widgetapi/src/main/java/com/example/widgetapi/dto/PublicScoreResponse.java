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
	private double co2;
	private double water;
	private double energy;
	private List<String> explanations;
	private Instant calculatedAt;

	public PublicScoreResponse() {
	}

	public PublicScoreResponse(UUID productId, UUID scoreId, double scoreValue, String scoreLetter,
			double confidence, double co2, double water, double energy,
			List<String> explanations, Instant calculatedAt) {
		this.productId = productId;
		this.scoreId = scoreId;
		this.scoreValue = scoreValue;
		this.scoreLetter = scoreLetter;
		this.confidence = confidence;
		this.co2 = co2;
		this.water = water;
		this.energy = energy;
		this.explanations = explanations;
		this.calculatedAt = calculatedAt;
	}

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

	public double getCo2() {
		return co2;
	}

	public void setCo2(double co2) {
		this.co2 = co2;
	}

	public double getWater() {
		return water;
	}

	public void setWater(double water) {
		this.water = water;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
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
