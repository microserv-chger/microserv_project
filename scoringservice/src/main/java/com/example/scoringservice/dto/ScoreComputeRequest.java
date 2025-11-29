package com.example.scoringservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class ScoreComputeRequest {

	@NotNull
	private UUID productId;

	@NotNull
	private Double co2Kg;

	@NotNull
	private Double waterLiters;

	@NotNull
	private Double energyMj;

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public Double getCo2Kg() {
		return co2Kg;
	}

	public void setCo2Kg(Double co2Kg) {
		this.co2Kg = co2Kg;
	}

	public Double getWaterLiters() {
		return waterLiters;
	}

	public void setWaterLiters(Double waterLiters) {
		this.waterLiters = waterLiters;
	}

	public Double getEnergyMj() {
		return energyMj;
	}

	public void setEnergyMj(Double energyMj) {
		this.energyMj = energyMj;
	}
}

