package com.business.businessdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.common")
@SpringBootApplication(scanBasePackages = {"com.business","com.common"})
public class BusinessDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessDemoApplication.class, args);
    }

}
