package com.example.scoringservice.dto;

import java.time.Instant;
import java.util.UUID;

public class LcaComputedEvent {

	private UUID productId;
	private UUID resultId;
	private double co2Kg;
	private double waterLiters;
	private double energyMj;
	private Instant calculatedAt;

	public LcaComputedEvent() {
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public UUID getResultId() {
		return resultId;
	}

	public void setResultId(UUID resultId) {
		this.resultId = resultId;
	}

	public double getCo2Kg() {
		return co2Kg;
	}

	public void setCo2Kg(double co2Kg) {
		this.co2Kg = co2Kg;
	}

	public double getWaterLiters() {
		return waterLiters;
	}

	public void setWaterLiters(double waterLiters) {
		this.waterLiters = waterLiters;
	}

	public double getEnergyMj() {
		return energyMj;
	}

	public void setEnergyMj(double energyMj) {
		this.energyMj = energyMj;
	}

	public Instant getCalculatedAt() {
		return calculatedAt;
	}

	public void setCalculatedAt(Instant calculatedAt) {
		this.calculatedAt = calculatedAt;
	}
}
