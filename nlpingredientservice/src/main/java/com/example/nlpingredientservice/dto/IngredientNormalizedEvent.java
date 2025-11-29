package com.example.nlpingredientservice.dto;

import java.util.List;
import java.util.UUID;

public class IngredientNormalizedEvent {

	private UUID productId;
	private List<NormalizedIngredientPayload> ingredients;

	public IngredientNormalizedEvent() {
	}

	public IngredientNormalizedEvent(UUID productId, List<NormalizedIngredientPayload> ingredients) {
		this.productId = productId;
		this.ingredients = ingredients;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public List<NormalizedIngredientPayload> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<NormalizedIngredientPayload> ingredients) {
		this.ingredients = ingredients;
	}

	public static class NormalizedIngredientPayload {
		private String name;
		private String category;
		private String ecoReference;
		private boolean organic;
		private double impactHint;

		public NormalizedIngredientPayload() {
		}

		public NormalizedIngredientPayload(String name, String category, String ecoReference,
				boolean organic, double impactHint) {
			this.name = name;
			this.category = category;
			this.ecoReference = ecoReference;
			this.organic = organic;
			this.impactHint = impactHint;
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
	}
}

