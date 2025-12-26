package com.example.nlpingredientservice.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for returning a product with its extracted ingredients
 */
public class ProductWithIngredients {

    private UUID productId;
    private String gtin;
    private String productName;
    private Instant processedAt;
    private List<IngredientDto> ingredients;

    public ProductWithIngredients() {
    }

    public ProductWithIngredients(UUID productId, String gtin, String productName, Instant processedAt,
            List<IngredientDto> ingredients) {
        this.productId = productId;
        this.gtin = gtin;
        this.productName = productName;
        this.processedAt = processedAt;
        this.ingredients = ingredients;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public List<IngredientDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientDto> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Nested DTO for ingredient details
     */
    public static class IngredientDto {
        private UUID id;
        private String name;
        private String category;
        private boolean organic;
        private double impactHint;
        private String ecoReference;
        private Instant extractedAt;

        public IngredientDto() {
        }

        public IngredientDto(UUID id, String name, String category, boolean organic, double impactHint,
                String ecoReference, Instant extractedAt) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.organic = organic;
            this.impactHint = impactHint;
            this.ecoReference = ecoReference;
            this.extractedAt = extractedAt;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
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

        public String getEcoReference() {
            return ecoReference;
        }

        public void setEcoReference(String ecoReference) {
            this.ecoReference = ecoReference;
        }

        public Instant getExtractedAt() {
            return extractedAt;
        }

        public void setExtractedAt(Instant extractedAt) {
            this.extractedAt = extractedAt;
        }
    }
}
