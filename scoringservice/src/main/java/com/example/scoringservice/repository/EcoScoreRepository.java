package com.example.scoringservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scoringservice.entity.EcoScore;

public interface EcoScoreRepository extends JpaRepository<EcoScore, UUID> {

	Optional<EcoScore> findByProductId(UUID productId);
}

