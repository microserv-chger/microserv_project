package com.example.lcaliteservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lcaliteservice.entity.ImpactFactor;

public interface ImpactFactorRepository extends JpaRepository<ImpactFactor, Long> {

	Optional<ImpactFactor> findByReference(String reference);

	Optional<ImpactFactor> findByCategory(String category);
}

