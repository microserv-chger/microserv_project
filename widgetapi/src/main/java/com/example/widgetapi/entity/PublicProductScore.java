package com.example.widgetapi.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "public_product_score")
public class PublicProductScore {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private UUID productId;

	@Column(nullable = false)
	private double scoreValue;

	@Column(nullable = false)
	private String scoreLetter;

	@Column(nullable = false)
	private double confidence;

	private Double co2;
	private Double water;
	private Double energy;

	@ElementCollection
	private List<String> explanations;

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

	public Double getCo2() {
		return co2;
	}

	public void setCo2(Double co2) {
		this.co2 = co2;
	}

	public Double getWater() {
		return water;
	}

	public void setWater(Double water) {
		this.water = water;
	}

	public Double getEnergy() {
		return energy;
	}

	public void setEnergy(Double energy) {
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
