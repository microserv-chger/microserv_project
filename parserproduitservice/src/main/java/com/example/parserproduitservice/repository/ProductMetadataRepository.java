package com.example.parserproduitservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.parserproduitservice.entity.ProductMetadata;

public interface ProductMetadataRepository extends JpaRepository<ProductMetadata, UUID> {

	Optional<ProductMetadata> findByGtin(String gtin);
}

