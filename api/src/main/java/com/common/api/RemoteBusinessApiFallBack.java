package com.common.api;

import org.springframework.stereotype.Component;

@Component
public class RemoteBusinessApiFallBack implements RemoteBusinessApi{
    @Override
    public String test() {
        return "业务服务错误";
    }
}
