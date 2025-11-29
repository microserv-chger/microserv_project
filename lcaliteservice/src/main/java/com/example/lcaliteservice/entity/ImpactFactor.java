package com.example.lcaliteservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "impact_factor")
public class ImpactFactor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String reference;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private double co2PerKg;

	@Column(nullable = false)
	private double waterPerKg;

	@Column(nullable = false)
	private double energyPerKg;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public double getCo2PerKg() {
		return co2PerKg;
	}

	public void setCo2PerKg(double co2PerKg) {
		this.co2PerKg = co2PerKg;
	}

	public double getWaterPerKg() {
		return waterPerKg;
	}

	public void setWaterPerKg(double waterPerKg) {
		this.waterPerKg = waterPerKg;
	}

	public double getEnergyPerKg() {
		return energyPerKg;
	}

	public void setEnergyPerKg(double energyPerKg) {
		this.energyPerKg = energyPerKg;
	}
}

