package com.example.authservice.security;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private final Key signingKey;
	private final long ttlSeconds;

	public JwtService(@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.ttl-seconds:3600}") long ttlSeconds) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
		this.ttlSeconds = ttlSeconds;
	}

	public String generateToken(String username, Set<String> roles) {
		Instant now = Instant.now();
		return Jwts.builder()
				.setSubject(username)
				.claim("roles", roles)
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
				.signWith(signingKey)
				.compact();
	}

	public Claims parse(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}

