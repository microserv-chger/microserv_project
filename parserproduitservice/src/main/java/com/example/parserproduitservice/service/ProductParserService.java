package com.example.parserproduitservice.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.parserproduitservice.client.MlServiceClient;
import com.example.parserproduitservice.dto.ProductParseRequest;
import com.example.parserproduitservice.dto.ProductParsedEvent;
import com.example.parserproduitservice.entity.ProductMetadata;
import com.example.parserproduitservice.messaging.ProductEventProducer;
import com.example.parserproduitservice.repository.ProductMetadataRepository;

@Service
public class ProductParserService {

	private final ProductMetadataRepository repository;
	private final ProductEventProducer eventProducer;
	private final MlServiceClient mlServiceClient;

	public ProductParserService(ProductMetadataRepository repository, ProductEventProducer eventProducer,
			MlServiceClient mlServiceClient) {
		this.repository = repository;
		this.eventProducer = eventProducer;
		this.mlServiceClient = mlServiceClient;
	}

	@Transactional
	public ProductMetadata parse(ProductParseRequest request) {
		ProductMetadata metadata = repository.findByGtin(request.getGtin())
				.orElseGet(ProductMetadata::new);

		metadata.setGtin(request.getGtin());
		metadata.setName(request.getName());
		metadata.setBrand(request.getBrand());
		metadata.setOriginCountry(request.getOriginCountry());
		metadata.setPackaging(request.getPackaging());

		// Extraction de texte via OCR/PDF si fourni (ML/IA)
		String extractedText = extractTextWithML(request);
		String finalText = extractedText != null && !extractedText.isEmpty() 
				? extractedText 
				: cleanText(request.getRawText());

		// Enrichir les métadonnées via NLP si le texte est disponible
		if (finalText != null && !finalText.isEmpty()) {
			Map<String, String> metadataFromML = mlServiceClient.extractMetadata(finalText);
			if (metadata.getBrand() == null && metadataFromML.containsKey("brand")) {
				metadata.setBrand(metadataFromML.get("brand"));
			}
			if (metadata.getOriginCountry() == null && metadataFromML.containsKey("origin")) {
				metadata.setOriginCountry(metadataFromML.get("origin"));
			}
		}

		metadata.setRawText(finalText);
		metadata.setParsedAt(Instant.now());

		ProductMetadata saved = repository.save(metadata);

		ProductParsedEvent event = new ProductParsedEvent(
				saved.getId(),
				saved.getGtin(),
				saved.getName(),
				saved.getBrand(),
				saved.getOriginCountry(),
				saved.getPackaging(),
				saved.getRawText(),
				saved.getParsedAt());
		eventProducer.publish(event);
		return saved;
	}

	private String cleanText(String rawText) {
		return rawText == null ? "" : rawText.replaceAll("\\s+", " ").trim();
	}

	/**
	 * Extrait le texte d'une image ou PDF via le service ML (OCR/Tesseract)
	 */
	private String extractTextWithML(ProductParseRequest request) {
		try {
			if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
				return mlServiceClient.extractTextFromImage(request.getImageBase64());
			}
			if (request.getPdfBase64() != null && !request.getPdfBase64().isEmpty()) {
				return mlServiceClient.extractTextFromPdf(request.getPdfBase64());
			}
		} catch (Exception e) {
			// Log l'erreur mais continue avec le texte brut si fourni
			System.err.println("ML extraction failed, using fallback: " + e.getMessage());
		}
		return null;
	}
}

