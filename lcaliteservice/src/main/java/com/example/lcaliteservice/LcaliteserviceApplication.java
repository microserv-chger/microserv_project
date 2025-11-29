package com.example.lcaliteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LcaliteserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LcaliteserviceApplication.class, args);
	}

}
