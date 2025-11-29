package com.example.lcaliteservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lcaliteservice.entity.LcaResult;

public interface LcaResultRepository extends JpaRepository<LcaResult, UUID> {

	Optional<LcaResult> findByProductId(UUID productId);
}

