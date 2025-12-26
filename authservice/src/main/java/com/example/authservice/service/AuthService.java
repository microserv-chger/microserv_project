package com.example.authservice.service;

import java.time.Instant;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.UserAccount;
import com.example.authservice.repository.UserAccountRepository;
import com.example.authservice.security.JwtService;

@Service
public class AuthService {

	private final UserAccountRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder,
			JwtService jwtService, AuthenticationManager authenticationManager) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		UserAccount account = new UserAccount();
		account.setUsername(request.getUsername());
		account.setEmail(request.getEmail());
		account.setPassword(passwordEncoder.encode(request.getPassword()));
		account.setRoles(Set.of("ROLE_USER"));
		account.setCreatedAt(Instant.now());
		UserAccount saved = repository.save(account);
		String token = jwtService.generateToken(saved.getUsername(), saved.getRoles());
		return new AuthResponse(token, saved.getRoles());
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		UserAccount account = repository.findByUsername(request.getUsername()).orElseThrow();
		String token = jwtService.generateToken(account.getUsername(), account.getRoles());
		return new AuthResponse(token, account.getRoles());
	}

	@Transactional(readOnly = true)
	public com.example.authservice.dto.UserDto getUserByUsername(String username) {
		UserAccount user = repository.findByUsername(username)
				.orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
						"User not found"));
		// Force initialization of lazy collection by copying it
		Set<String> initializedRoles = new java.util.HashSet<>(user.getRoles());
		return new com.example.authservice.dto.UserDto(user.getId(), user.getUsername(), user.getEmail(),
				initializedRoles, user.getCreatedAt());
	}
}
