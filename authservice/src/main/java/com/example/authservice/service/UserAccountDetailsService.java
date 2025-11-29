package com.example.authservice.service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.authservice.repository.UserAccountRepository;

@Service
public class UserAccountDetailsService implements UserDetailsService {

	private final UserAccountRepository repository;

	public UserAccountDetailsService(UserAccountRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return repository.findByUsername(username)
				.map(user -> new User(user.getUsername(), user.getPassword(),
						user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())))
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}

