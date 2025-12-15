package com.example.parserproduitservice.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload representing the metadata extracted from a product file.
 */
public class ProductParseRequest {

	@NotBlank
	private String gtin;

	@NotBlank
	private String name;

	private String brand;

	private String originCountry;

	private String packaging;

	private String rawText;

	// Support pour OCR/PDF via ML service
	private String imageBase64; // Image encodée en base64 pour OCR
	private String pdfBase64;   // PDF encodé en base64 pour parsing

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

	public String getRawText() {
		return rawText;
	}

	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	public String getImageBase64() {
		return imageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

	public String getPdfBase64() {
		return pdfBase64;
	}

	public void setPdfBase64(String pdfBase64) {
		this.pdfBase64 = pdfBase64;
	}
}

