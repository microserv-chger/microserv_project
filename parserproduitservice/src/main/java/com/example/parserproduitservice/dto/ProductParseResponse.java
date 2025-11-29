package com.example.parserproduitservice.dto;

import java.util.UUID;

public class ProductParseResponse {

	private final UUID productId;
	private final String status;

	public ProductParseResponse(UUID productId, String status) {
		this.productId = productId;
		this.status = status;
	}

	public UUID getProductId() {
		return productId;
	}

	public String getStatus() {
		return status;
	}
}

