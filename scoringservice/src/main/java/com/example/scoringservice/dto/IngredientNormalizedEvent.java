package com.example.scoringservice.dto;

import java.util.List;
import java.util.UUID;

public class IngredientNormalizedEvent {
    private UUID productId;
    private List<Object> ingredients;

    public IngredientNormalizedEvent() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public List<Object> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Object> ingredients) {
        this.ingredients = ingredients;
    }
}
