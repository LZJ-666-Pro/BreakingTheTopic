package com.poti.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.poti.gateway", "com.poti.common"})
public class PotiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotiGatewayApplication.class, args);
	}

}
