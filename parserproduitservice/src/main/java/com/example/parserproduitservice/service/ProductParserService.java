package com.example.parserproduitservice.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.parserproduitservice.dto.ProductParseRequest;
import com.example.parserproduitservice.dto.ProductParsedEvent;
import com.example.parserproduitservice.entity.ProductMetadata;
import com.example.parserproduitservice.messaging.ProductEventProducer;
import com.example.parserproduitservice.repository.ProductMetadataRepository;

@Service
public class ProductParserService {

	private final ProductMetadataRepository repository;
	private final ProductEventProducer eventProducer;

	public ProductParserService(ProductMetadataRepository repository, ProductEventProducer eventProducer) {
		this.repository = repository;
		this.eventProducer = eventProducer;
	}

	@Transactional
	public ProductMetadata parse(ProductParseRequest request) {
		ProductMetadata metadata = repository.findByGtin(request.getGtin())
				.orElseGet(ProductMetadata::new);

		metadata.setGtin(request.getGtin());
		metadata.setName(request.getName());
		metadata.setBrand(request.getBrand());
		metadata.setOriginCountry(request.getOriginCountry());
		metadata.setPackaging(request.getPackaging());
		metadata.setRawText(cleanText(request.getRawText()));
		metadata.setParsedAt(Instant.now());

		ProductMetadata saved = repository.save(metadata);

		ProductParsedEvent event = new ProductParsedEvent(
				saved.getId(),
				saved.getGtin(),
				saved.getName(),
				saved.getBrand(),
				saved.getOriginCountry(),
				saved.getPackaging(),
				saved.getRawText(),
				saved.getParsedAt());
		eventProducer.publish(event);
		return saved;
	}

	private String cleanText(String rawText) {
		return rawText == null ? "" : rawText.replaceAll("\\s+", " ").trim();
	}
}

