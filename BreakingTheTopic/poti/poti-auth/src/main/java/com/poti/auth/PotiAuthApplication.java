package com.poti.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.poti"})
public class PotiAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotiAuthApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setSupportedMediaTypes(List.of(
			org.springframework.http.MediaType.TEXT_PLAIN,
			org.springframework.http.MediaType.APPLICATION_JSON
		));
		
		restTemplate.getMessageConverters().removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
		restTemplate.getMessageConverters().add(0, jsonConverter);
		restTemplate.getMessageConverters().add(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		
		return restTemplate;
	}

}
