package com.poti.wrongbook;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.poti.wrongbook.mapper")
public class PotiWrongbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotiWrongbookApplication.class, args);
	}

}
