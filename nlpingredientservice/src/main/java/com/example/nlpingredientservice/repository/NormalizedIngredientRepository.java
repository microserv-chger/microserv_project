package com.example.nlpingredientservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nlpingredientservice.entity.NormalizedIngredient;

public interface NormalizedIngredientRepository extends JpaRepository<NormalizedIngredient, UUID> {

	List<NormalizedIngredient> findByProductId(UUID productId);
}

