package com.example.scoringservice.dto;

import java.time.Instant;
import java.util.UUID;

public class ProductParsedEvent {
    private UUID productId;
    private String gtin;
    private String name;
    private Instant parsedAt;

    public ProductParsedEvent() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getParsedAt() {
        return parsedAt;
    }

    public void setParsedAt(Instant parsedAt) {
        this.parsedAt = parsedAt;
    }
}
