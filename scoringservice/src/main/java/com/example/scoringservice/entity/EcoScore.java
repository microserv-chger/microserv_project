package com.example.scoringservice.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "eco_score")
public class EcoScore {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private UUID productId;

	@Column(nullable = false)
	private double scoreValue;

	@Column(nullable = false, length = 1)
	private String scoreLetter;

	@Column(nullable = false)
	private double confidence;

	@Column(nullable = false)
	private double co2;

	@Column(nullable = false)
	private double water;

	@Column(nullable = false)
	private double energy;

	@ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
	private java.util.List<String> explanations;

	@Column(nullable = false)
	private Instant calculatedAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
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

	public java.util.List<String> getExplanations() {
		return explanations;
	}

	public void setExplanations(java.util.List<String> explanations) {
		this.explanations = explanations;
	}

	public Instant getCalculatedAt() {
		return calculatedAt;
	}

	public void setCalculatedAt(Instant calculatedAt) {
		this.calculatedAt = calculatedAt;
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
}
