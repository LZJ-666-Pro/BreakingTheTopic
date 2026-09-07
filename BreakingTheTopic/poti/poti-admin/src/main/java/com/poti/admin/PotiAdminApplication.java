package com.poti.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@MapperScan("com.poti.admin.mapper")
@ComponentScan(basePackages = {"com.poti.admin", "com.poti.common"})
public class PotiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(PotiAdminApplication.class, args);
    }

}
