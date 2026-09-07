package com.poti.potimonitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient   // 可选，如希望监控中心也注册到 Nacos
public class PotiMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PotiMonitorApplication.class, args);
    }

}
