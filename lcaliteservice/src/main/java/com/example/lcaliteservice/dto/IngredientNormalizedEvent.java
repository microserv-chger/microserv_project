package com.example.lcaliteservice.dto;

import java.util.List;
import java.util.UUID;

public class IngredientNormalizedEvent {

	private UUID productId;
	private List<NormalizedIngredient> ingredients;

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public List<NormalizedIngredient> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<NormalizedIngredient> ingredients) {
		this.ingredients = ingredients;
	}

	public static class NormalizedIngredient {
		private String name;
		private String category;
		private String ecoReference;
		private boolean organic;
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
	}
}

