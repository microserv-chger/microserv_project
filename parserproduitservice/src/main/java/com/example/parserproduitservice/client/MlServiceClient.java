package com.example.parserproduitservice.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client REST pour appeler le microservice ML (Python) pour OCR et extraction de métadonnées
 */
@Component
public class MlServiceClient {

	private final RestTemplate restTemplate;
	private final String mlServiceUrl;

	public MlServiceClient(RestTemplate restTemplate,
			@Value("${ml.service.url:http://ml-service:8086}") String mlServiceUrl) {
		this.restTemplate = restTemplate;
		this.mlServiceUrl = mlServiceUrl;
	}

	/**
	 * Extrait le texte d'une image via OCR (Tesseract)
	 */
	public String extractTextFromImage(String imageBase64) {
		try {
			Map<String, String> requestBody = Map.of("image", imageBase64);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

			@SuppressWarnings("unchecked")
			ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
					mlServiceUrl + "/ocr/image", request, (Class<Map<String, Object>>) (Class<?>) Map.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return (String) response.getBody().get("text");
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to extract text from image via ML service: " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Extrait le texte d'un PDF
	 */
	public String extractTextFromPdf(String pdfBase64) {
		try {
			Map<String, String> requestBody = Map.of("pdf", pdfBase64);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

			@SuppressWarnings("unchecked")
			ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
					mlServiceUrl + "/ocr/pdf", request, (Class<Map<String, Object>>) (Class<?>) Map.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return (String) response.getBody().get("text");
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to extract text from PDF via ML service: " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Extrait les métadonnées (marque, origine, etc.) d'un texte via NLP
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> extractMetadata(String text) {
		try {
			Map<String, String> requestBody = Map.of("text", text);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

			@SuppressWarnings("unchecked")
			ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
					mlServiceUrl + "/nlp/extract-metadata", request, (Class<Map<String, Object>>) (Class<?>) Map.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				Object metadataObj = response.getBody().get("metadata");
				if (metadataObj instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, String> metadata = (Map<String, String>) metadataObj;
					return metadata;
				}
			}
		} catch (Exception e) {
			// Log mais ne pas faire échouer le parsing si ML échoue
			System.err.println("Failed to extract metadata via ML service: " + e.getMessage());
		}
		return Map.of();
	}
}

