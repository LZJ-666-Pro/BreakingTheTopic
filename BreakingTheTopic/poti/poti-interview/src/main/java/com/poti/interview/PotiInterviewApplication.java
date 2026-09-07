package com.poti.interview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.poti.interview.mapper")
@EnableFeignClients(basePackages = {"com.poti.interview.feign"})
public class PotiInterviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(PotiInterviewApplication.class, args);
    }

}
