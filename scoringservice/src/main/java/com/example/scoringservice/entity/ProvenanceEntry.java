package com.example.scoringservice.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "provenance_entries")
public class ProvenanceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String stepName; // PARSING, NLP, LCA, SCORING

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON or descriptive string (Model version, Dataset ID, MinIO link)

    @Column(nullable = false)
    private Instant timestamp;

    public ProvenanceEntry() {
    }

    public ProvenanceEntry(UUID productId, String stepName, String status, String metadata, Instant timestamp) {
        this.productId = productId;
        this.stepName = stepName;
        this.status = status;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

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

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
