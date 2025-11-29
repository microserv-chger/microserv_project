package com.example.lcaliteservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.lcaliteservice.entity.ImpactFactor;
import com.example.lcaliteservice.repository.ImpactFactorRepository;

@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner impactFactorSeeder(ImpactFactorRepository repository) {
		return args -> {
			if (repository.count() > 0) {
				return;
			}

			repository.save(factor("DAIRY", 9.2, 1000, 15));
			repository.save(factor("SWEETENER", 3.5, 600, 5));
			repository.save(factor("PACKAGING", 2.2, 100, 8));
			repository.save(factor("GLASS", 1.8, 150, 6));
			repository.save(factor("OTHER", 1.0, 200, 4));
		};
	}

	private ImpactFactor factor(String category, double co2, double water, double energy) {
		ImpactFactor factor = new ImpactFactor();
		factor.setCategory(category);
		factor.setReference("FACTOR_" + category);
		factor.setCo2PerKg(co2);
		factor.setWaterPerKg(water);
		factor.setEnergyPerKg(energy);
		return factor;
	}
}

