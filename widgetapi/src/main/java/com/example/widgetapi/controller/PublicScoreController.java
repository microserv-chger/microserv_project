package com.example.widgetapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.widgetapi.dto.PublicScoreResponse;
import com.example.widgetapi.service.PublicScoreService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/public/product")
public class PublicScoreController {

	private final PublicScoreService scoreService;

	public PublicScoreController(PublicScoreService scoreService) {
		this.scoreService = scoreService;
	}

	@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
	public String index() {
		return renderCatalogHtml();
	}

	@GetMapping(value = "/all", produces = MediaType.TEXT_HTML_VALUE)
	public String listProductsHtml() {
		return renderCatalogHtml();
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PublicScoreResponse>> listProductsJson() {
		return ResponseEntity.ok(scoreService.getAllScores());
	}

	@GetMapping("/{productId}")
	public ResponseEntity<PublicScoreResponse> getScore(@PathVariable UUID productId) {
		return scoreService.findScore(productId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	private String renderCatalogHtml() {
		List<PublicScoreResponse> scores = scoreService.getAllScores();

		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head>");
		html.append("<meta charset='UTF-8'>");
		html.append("<title>Ecolabel Public Catalog</title>");
		html.append("<style>");
		html.append(
				"body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #fdfdfd; color: #333; margin: 0; padding: 40px; }");
		html.append(
				".container { max-width: 900px; margin: 0 auto; background: white; padding: 40px; border-radius: 16px; box-shadow: 0 15px 35px rgba(0,0,0,0.05); border: 1px solid #eee; }");
		html.append(
				"h1 { color: #1b5e20; border-bottom: 3px solid #e8f5e9; padding-bottom: 20px; margin-top: 0; font-weight: 800; }");
		html.append("table { width: 100%; border-collapse: separate; border-spacing: 0 10px; margin-top: 30px; }");
		html.append(
				"th { text-align: left; padding: 15px; color: #666; text-transform: uppercase; font-size: 11px; letter-spacing: 1.5px; border-bottom: 1px solid #eee; }");
		html.append(
				"td { padding: 20px 15px; border-top: 1px solid #fafafa; border-bottom: 1px solid #fafafa; background: #fff; }");
		html.append("tr:hover td { background: #f9fdf9; }");
		html.append(
				".score-badge { display: inline-block; width: 40px; height: 40px; line-height: 40px; text-align: center; border-radius: 10px; font-weight: 900; color: white; font-size: 20px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }");
		html.append(
				".link-btn { display: inline-block; padding: 8px 16px; background: #e3f2fd; color: #1976d2; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 13px; transition: 0.2s; }");
		html.append(".link-btn:hover { background: #1976d2; color: white; transform: translateY(-2px); }");
		html.append(".uuid { font-family: 'Courier New', monospace; color: #888; font-size: 12px; }");
		html.append("</style></head><body>");

		html.append("<div class='container'>");
		html.append("<h1>🌿 Catalogue Public EcoLabel</h1>");
		html.append(
				"<p style='color:#666; font-size:16px;'>Explorez les produits certifiés et leurs impacts environnementaux.</p>");

		html.append("<table><thead><tr><th>Produit</th><th>Score</th><th>Action</th></tr></thead><tbody>");

		for (PublicScoreResponse s : scores) {
			String color = "#9e9e9e";
			if ("A".equals(s.getScoreLetter()))
				color = "#2e7d32";
			else if ("B".equals(s.getScoreLetter()))
				color = "#4caf50";
			else if ("C".equals(s.getScoreLetter()))
				color = "#fbc02d";
			else if ("D".equals(s.getScoreLetter()))
				color = "#f57c00";
			else if ("E".equals(s.getScoreLetter()))
				color = "#d32f2f";

			html.append("<tr>");
			html.append("<td><div class='uuid'>").append(s.getProductId()).append("</div></td>");
			html.append("<td><span class='score-badge' style='background:").append(color).append(";'>")
					.append(s.getScoreLetter()).append("</span></td>");
			html.append("<td><a class='link-btn' href='/public/product/").append(s.getProductId())
					.append("'>Voir JSON</a></td>");
			html.append("</tr>");
		}

		if (scores.isEmpty()) {
			html.append(
					"<tr><td colspan='3' style='text-align:center; padding: 60px; color: #bbb;'>Aucun produit trouvé dans le catalogue public.</td></tr>");
		}

		html.append("</tbody></table>");
		html.append(
				"<footer style='margin-top:50px; padding-top:20px; border-top: 1px solid #eee; display: flex; justify-content: space-between; align-items: center;'>");
		html.append("<span style='font-size:12px; color:#aaa;'>Widget API v1.1 • Architecture Microservices</span>");
		html.append("<span style='font-size:12px; color:#aaa;'>Mise à jour via Kafka</span>");
		html.append("</footer>");
		html.append("</div></body></html>");

		return html.toString();
	}
}
