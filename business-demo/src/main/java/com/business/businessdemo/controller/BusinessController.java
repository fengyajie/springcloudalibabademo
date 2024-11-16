package com.business.businessdemo.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RefreshScope
@RequestMapping("/business")
@RestController
public class BusinessController {

    @Value("${xxl}")
    private String xxl;

    @GetMapping("/test")
    public String test(){
        log.info("log{}",xxl);
        return xxl;
    }

}
