package com.example.authservice.dto;

import java.util.Set;

public class AuthResponse {

	private final String token;
	private final String type;
	private final Set<String> roles;

	public AuthResponse(String token, Set<String> roles) {
		this.token = token;
		this.type = "Bearer";
		this.roles = roles;
	}

	public String getToken() {
		return token;
	}

	public String getType() {
		return type;
	}

	public Set<String> getRoles() {
		return roles;
	}
}

