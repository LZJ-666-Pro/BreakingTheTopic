package com.poti.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.poti.agent.feign")
@MapperScan("com.poti.agent.mapper")
public class PotiAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotiAgentApplication.class, args);
	}

}
