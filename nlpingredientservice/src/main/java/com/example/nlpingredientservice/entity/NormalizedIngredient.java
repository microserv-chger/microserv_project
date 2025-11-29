package com.example.nlpingredientservice.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "normalized_ingredient")
public class NormalizedIngredient {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private UUID productId;

	@Column(nullable = false)
	private String name;

	private String category;

	private String ecoReference;

	private boolean organic;

	private double impactHint;

	@Column(nullable = false)
	private Instant extractedAt;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getEcoReference() {
		return ecoReference;
	}

	public void setEcoReference(String ecoReference) {
		this.ecoReference = ecoReference;
	}

	public boolean isOrganic() {
		return organic;
	}

	public void setOrganic(boolean organic) {
		this.organic = organic;
	}

	public double getImpactHint() {
		return impactHint;
	}

	public void setImpactHint(double impactHint) {
		this.impactHint = impactHint;
	}

	public Instant getExtractedAt() {
		return extractedAt;
	}

	public void setExtractedAt(Instant extractedAt) {
		this.extractedAt = extractedAt;
	}
}

