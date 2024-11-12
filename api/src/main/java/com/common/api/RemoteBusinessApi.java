package com.common.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="business-demo",fallback = RemoteBusinessApiFallBack.class)
@Component
public interface RemoteBusinessApi {

    @GetMapping("/business/test")
    String test();
}
