package com.webflux.client.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootWebfluxClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxClientApplication.class, args);
	}

}
