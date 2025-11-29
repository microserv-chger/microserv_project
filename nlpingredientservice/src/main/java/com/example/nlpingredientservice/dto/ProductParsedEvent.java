package com.example.nlpingredientservice.dto;

import java.time.Instant;
import java.util.UUID;

public class ProductParsedEvent {

	private UUID productId;
	private String gtin;
	private String name;
	private String brand;
	private String originCountry;
	private String packaging;
	private String normalizedText;
	private Instant parsedAt;

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public String getGtin() {
		return gtin;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getOriginCountry() {
		return originCountry;
	}

	public void setOriginCountry(String originCountry) {
		this.originCountry = originCountry;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	public String getNormalizedText() {
		return normalizedText;
	}

	public void setNormalizedText(String normalizedText) {
		this.normalizedText = normalizedText;
	}

	public Instant getParsedAt() {
		return parsedAt;
	}

	public void setParsedAt(Instant parsedAt) {
		this.parsedAt = parsedAt;
	}
}

