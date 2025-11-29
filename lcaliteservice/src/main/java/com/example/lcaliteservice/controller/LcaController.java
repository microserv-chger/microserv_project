package com.example.lcaliteservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lcaliteservice.dto.LcaCalculationRequest;
import com.example.lcaliteservice.dto.LcaResultDto;
import com.example.lcaliteservice.repository.LcaResultRepository;
import com.example.lcaliteservice.service.LcaCalculatorService;

@RestController
@RequestMapping("/lca")
public class LcaController {

	private final LcaCalculatorService calculatorService;
	private final LcaResultRepository resultRepository;

	public LcaController(LcaCalculatorService calculatorService, LcaResultRepository resultRepository) {
		this.calculatorService = calculatorService;
		this.resultRepository = resultRepository;
	}

	@PostMapping("/calc")
	public ResponseEntity<LcaResultDto> calculate(@Validated @RequestBody LcaCalculationRequest request) {
		return ResponseEntity.ok(calculatorService.calculate(request));
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<LcaResultDto> getByProduct(@PathVariable UUID productId) {
		return resultRepository.findByProductId(productId)
				.map(result -> new LcaResultDto(result.getId(), result.getProductId(),
						result.getTotalCo2Kg(), result.getTotalWaterLiters(), result.getTotalEnergyMj(),
						result.getCalculatedAt()))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}

