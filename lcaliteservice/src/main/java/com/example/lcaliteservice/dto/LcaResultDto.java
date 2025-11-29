package com.example.lcaliteservice.dto;

import java.time.Instant;
import java.util.UUID;

public class LcaResultDto {

	private UUID resultId;
	private UUID productId;
	private double totalCo2Kg;
	private double totalWaterLiters;
	private double totalEnergyMj;
	private Instant calculatedAt;

	public LcaResultDto(UUID resultId, UUID productId, double totalCo2Kg, double totalWaterLiters,
			double totalEnergyMj, Instant calculatedAt) {
		this.resultId = resultId;
		this.productId = productId;
		this.totalCo2Kg = totalCo2Kg;
		this.totalWaterLiters = totalWaterLiters;
		this.totalEnergyMj = totalEnergyMj;
		this.calculatedAt = calculatedAt;
	}

	public UUID getResultId() {
		return resultId;
	}

	public UUID getProductId() {
		return productId;
	}

	public double getTotalCo2Kg() {
		return totalCo2Kg;
	}

	public double getTotalWaterLiters() {
		return totalWaterLiters;
	}

	public double getTotalEnergyMj() {
		return totalEnergyMj;
	}

	public Instant getCalculatedAt() {
		return calculatedAt;
	}
}

