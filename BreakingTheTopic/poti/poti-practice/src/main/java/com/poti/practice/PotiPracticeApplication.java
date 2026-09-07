package com.poti.practice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.poti.practice.mapper")
@EnableFeignClients(basePackages = {"com.poti.practice.feign"})
public class PotiPracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotiPracticeApplication.class, args);
	}

}
