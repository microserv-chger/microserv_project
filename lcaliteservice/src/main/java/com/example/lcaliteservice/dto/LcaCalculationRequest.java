package com.example.lcaliteservice.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class LcaCalculationRequest {

	@NotNull
	private UUID productId;

	@NotNull
	private List<IngredientImpact> ingredients;

	private double transportKm;

	private String transportMode = "road";

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public List<IngredientImpact> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<IngredientImpact> ingredients) {
		this.ingredients = ingredients;
	}

	public double getTransportKm() {
		return transportKm;
	}

	public void setTransportKm(double transportKm) {
		this.transportKm = transportKm;
	}

	public String getTransportMode() {
		return transportMode;
	}

	public void setTransportMode(String transportMode) {
		this.transportMode = transportMode;
	}

	public static class IngredientImpact {
		private String name;
		private String category;
		private double impactHint;

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

		public double getImpactHint() {
			return impactHint;
		}

		public void setImpactHint(double impactHint) {
			this.impactHint = impactHint;
		}
	}
}

