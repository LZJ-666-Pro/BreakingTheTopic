package com.poti.favorite;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.poti.favorite.mapper")
public class PotiFavoriteApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotiFavoriteApplication.class, args);
	}

}
