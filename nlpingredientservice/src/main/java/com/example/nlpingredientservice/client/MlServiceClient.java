package com.example.nlpingredientservice.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client REST pour appeler le microservice ML (Python) pour l'extraction NLP d'ingrédients
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
	 * Extrait les ingrédients d'un texte via ML (spaCy + BERT)
	 * Retourne une liste d'ingrédients avec catégories et confiance
	 */
	@SuppressWarnings("unchecked")
	public MlIngredientResponse extractIngredients(String text) {
		try {
			Map<String, String> requestBody = Map.of("text", text);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

			@SuppressWarnings("unchecked")
			ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
					mlServiceUrl + "/nlp/extract-ingredients", request, (Class<Map<String, Object>>) (Class<?>) Map.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				Map<String, Object> body = response.getBody();
				List<Map<String, Object>> ingredients = (List<Map<String, Object>>) body.get("ingredients");
				Boolean organic = (Boolean) body.get("organic");
				return new MlIngredientResponse(ingredients, organic != null ? organic : false);
			}
		} catch (Exception e) {
			// Log mais ne pas faire échouer si ML échoue - on utilisera le fallback
			System.err.println("ML ingredient extraction failed, using fallback: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Réponse du service ML pour l'extraction d'ingrédients
	 */
	public static class MlIngredientResponse {
		private final List<Map<String, Object>> ingredients;
		private final boolean organic;

		public MlIngredientResponse(List<Map<String, Object>> ingredients, boolean organic) {
			this.ingredients = ingredients;
			this.organic = organic;
		}

		public List<Map<String, Object>> getIngredients() {
			return ingredients;
		}

		public boolean isOrganic() {
			return organic;
		}
	}
}

