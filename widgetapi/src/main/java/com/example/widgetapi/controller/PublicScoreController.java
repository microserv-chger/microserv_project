package com.example.widgetapi.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.widgetapi.dto.PublicScoreResponse;
import com.example.widgetapi.service.PublicScoreService;

@RestController
@RequestMapping("/public/product")
public class PublicScoreController {

	private final PublicScoreService scoreService;

	public PublicScoreController(PublicScoreService scoreService) {
		this.scoreService = scoreService;
	}

	@GetMapping("/{productId}")
	public ResponseEntity<PublicScoreResponse> getScore(@PathVariable UUID productId) {
		return scoreService.findScore(productId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}

