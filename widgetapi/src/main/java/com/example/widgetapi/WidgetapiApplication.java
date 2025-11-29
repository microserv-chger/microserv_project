package com.example.widgetapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WidgetapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WidgetapiApplication.class, args);
	}

}
