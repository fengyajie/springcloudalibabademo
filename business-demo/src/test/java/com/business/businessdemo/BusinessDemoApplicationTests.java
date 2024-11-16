package com.business.businessdemo;

import com.business.businessdemo.controller.service.BusnessMqSendService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class BusinessDemoApplicationTests {

    @Autowired
    private BusnessMqSendService busnessMqSendService;

    @Test
    void contextLoads() {
    }

    @Test
    public void sendSyncMq(){
        busnessMqSendService.sync("测试消息");
    }

}
