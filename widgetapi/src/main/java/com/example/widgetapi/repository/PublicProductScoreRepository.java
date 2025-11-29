package com.example.widgetapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.widgetapi.entity.PublicProductScore;

public interface PublicProductScoreRepository extends JpaRepository<PublicProductScore, UUID> {

	Optional<PublicProductScore> findByProductId(UUID productId);
}

