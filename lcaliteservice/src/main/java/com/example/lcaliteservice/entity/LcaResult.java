package com.example.lcaliteservice.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lca_result")
public class LcaResult {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private UUID productId;

	@Column(nullable = false)
	private double totalCo2Kg;

	@Column(nullable = false)
	private double totalWaterLiters;

	@Column(nullable = false)
	private double totalEnergyMj;

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

	public double getTotalCo2Kg() {
		return totalCo2Kg;
	}

	public void setTotalCo2Kg(double totalCo2Kg) {
		this.totalCo2Kg = totalCo2Kg;
	}

	public double getTotalWaterLiters() {
		return totalWaterLiters;
	}

	public void setTotalWaterLiters(double totalWaterLiters) {
		this.totalWaterLiters = totalWaterLiters;
	}

	public double getTotalEnergyMj() {
		return totalEnergyMj;
	}

	public void setTotalEnergyMj(double totalEnergyMj) {
		this.totalEnergyMj = totalEnergyMj;
	}

	public Instant getCalculatedAt() {
		return calculatedAt;
	}

	public void setCalculatedAt(Instant calculatedAt) {
		this.calculatedAt = calculatedAt;
	}
}

