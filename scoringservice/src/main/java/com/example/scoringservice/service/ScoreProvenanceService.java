package com.example.scoringservice.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.scoringservice.dto.IngredientNormalizedEvent;
import com.example.scoringservice.dto.LcaComputedEvent;
import com.example.scoringservice.dto.ProductParsedEvent;
import com.example.scoringservice.dto.ScorePublishedEvent;
import com.example.scoringservice.entity.ProvenanceEntry;
import com.example.scoringservice.repository.ProvenanceEntryRepository;

@Service
public class ScoreProvenanceService {

    private static final Logger log = LoggerFactory.getLogger(ScoreProvenanceService.class);
    private final ProvenanceEntryRepository repository;

    public ScoreProvenanceService(ProvenanceEntryRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "product.parsed", groupId = "scoring-provenance-group")
    public void handleProductParsed(ProductParsedEvent event) {
        log.info("Provenance: Product parsed {}", event.getProductId());
        saveEntry(event.getProductId(), "PARSING", "SUCCESS",
                "Source: OpenFoodFacts / OCR. Model: Tesseract v5.0. Storage: MinIO bucket 'raw-data'.");
    }

    @KafkaListener(topics = "ingredients.normalized", groupId = "scoring-provenance-group")
    public void handleIngredientsNormalized(IngredientNormalizedEvent event) {
        log.info("Provenance: Ingredients normalized for {}", event.getProductId());
        saveEntry(event.getProductId(), "NLP", "SUCCESS",
                "Model: BERT-base-French. Dataset: custom-ingredients-v1. Version: DVC-hash-88a2.");
    }

    @KafkaListener(topics = "lca.completed", groupId = "scoring-provenance-group")
    public void handleLcaCompleted(LcaComputedEvent event) {
        log.info("Provenance: LCA completed for {}", event.getProductId());
        saveEntry(event.getProductId(), "LCA", "SUCCESS",
                "Engine: LcaLite v1.0. Database: EcoInvent v3.8 (via MLflow). CO2 Ref: ISO 14067.");
    }

    @KafkaListener(topics = "score.published", groupId = "scoring-provenance-group")
    public void handleScorePublished(ScorePublishedEvent event) {
        log.info("Provenance: Score published for {}", event.getProductId());
        saveEntry(event.getProductId(), "SCORING", "SUCCESS",
                "Engine: ScoringEngine v2.0. Weighting: 50/25/25. Result: " + event.getScoreLetter());
    }

    private void saveEntry(UUID productId, String step, String status, String metadata) {
        ProvenanceEntry entry = new ProvenanceEntry(productId, step, status, metadata, Instant.now());
        repository.save(entry);
    }
}
