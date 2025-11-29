package com.example.lcaliteservice.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lcaliteservice.dto.LcaCalculationRequest;
import com.example.lcaliteservice.dto.LcaComputedEvent;
import com.example.lcaliteservice.dto.LcaResultDto;
import com.example.lcaliteservice.entity.ImpactFactor;
import com.example.lcaliteservice.entity.LcaResult;
import com.example.lcaliteservice.messaging.LcaEventProducer;
import com.example.lcaliteservice.repository.ImpactFactorRepository;
import com.example.lcaliteservice.repository.LcaResultRepository;

@Service
public class LcaCalculatorService {

	private final ImpactFactorRepository factorRepository;
	private final LcaResultRepository resultRepository;
	private final LcaEventProducer eventProducer;

	public LcaCalculatorService(ImpactFactorRepository factorRepository,
			LcaResultRepository resultRepository,
			LcaEventProducer eventProducer) {
		this.factorRepository = factorRepository;
		this.resultRepository = resultRepository;
		this.eventProducer = eventProducer;
	}

	@Transactional
	public LcaResultDto calculate(LcaCalculationRequest request) {
		double totalCo2 = 0;
		double totalWater = 0;
		double totalEnergy = 0;

		for (LcaCalculationRequest.IngredientImpact ingredient : request.getIngredients()) {
			ImpactFactor factor = factorRepository.findByCategory(ingredient.getCategory())
					.orElseGet(() -> factorRepository.findByCategory("OTHER").orElseThrow());
			totalCo2 += factor.getCo2PerKg() * ingredient.getImpactHint();
			totalWater += factor.getWaterPerKg() * ingredient.getImpactHint();
			totalEnergy += factor.getEnergyPerKg() * ingredient.getImpactHint();
		}

		double transportMultiplier = transportMultiplier(request.getTransportMode());
		totalCo2 += request.getTransportKm() * 0.1 * transportMultiplier;
		totalWater += request.getTransportKm() * 0.02 * transportMultiplier;
		totalEnergy += request.getTransportKm() * 0.05 * transportMultiplier;

		LcaResult result = resultRepository.findByProductId(request.getProductId()).orElseGet(LcaResult::new);
		result.setProductId(request.getProductId());
		result.setTotalCo2Kg(round(totalCo2));
		result.setTotalWaterLiters(round(totalWater));
		result.setTotalEnergyMj(round(totalEnergy));
		result.setCalculatedAt(Instant.now());
		LcaResult saved = resultRepository.save(result);

		eventProducer.publish(new LcaComputedEvent(saved.getProductId(), saved.getId(), saved.getTotalCo2Kg(),
				saved.getTotalWaterLiters(), saved.getTotalEnergyMj(), saved.getCalculatedAt()));

		return new LcaResultDto(saved.getId(), saved.getProductId(), saved.getTotalCo2Kg(),
				saved.getTotalWaterLiters(), saved.getTotalEnergyMj(), saved.getCalculatedAt());
	}

	@Transactional
	public void processIngredientEvent(UUID productId, List<LcaCalculationRequest.IngredientImpact> ingredients) {
		LcaCalculationRequest request = new LcaCalculationRequest();
		request.setProductId(productId);
		request.setIngredients(ingredients);
		request.setTransportKm(0);
		request.setTransportMode("road");
		calculate(request);
	}

	private double transportMultiplier(String mode) {
		return switch (mode.toLowerCase()) {
			case "air" -> 5;
			case "sea" -> 0.8;
			default -> 1.5;
		};
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}

