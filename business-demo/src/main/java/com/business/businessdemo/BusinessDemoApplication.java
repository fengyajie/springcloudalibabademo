package com.business.businessdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


//@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.business"})
public class BusinessDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessDemoApplication.class, args);
    }

}
