package com.poti.wrongbook;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.poti.wrongbook.mapper")
@EnableFeignClients(basePackages = {"com.poti.wrongbook.feign"})
public class PotiWrongbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotiWrongbookApplication.class, args);
	}

}
