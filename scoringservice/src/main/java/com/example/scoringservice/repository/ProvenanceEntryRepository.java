package com.example.scoringservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.scoringservice.entity.ProvenanceEntry;

@Repository
public interface ProvenanceEntryRepository extends JpaRepository<ProvenanceEntry, UUID> {
    List<ProvenanceEntry> findByProductIdOrderByTimestampAsc(UUID productId);
}
