package com.gateway.gatewaydemo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/fallbackA")
    public String fallbackA() {
        JSONObject response = new JSONObject();
        response.put("code","505");
        response.put("msg","服务不可用");
        return JSON.toJSONString(response);
    }
}
