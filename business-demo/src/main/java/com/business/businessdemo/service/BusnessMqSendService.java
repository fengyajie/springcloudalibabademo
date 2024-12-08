package com.business.businessdemo.service;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BusnessMqSendService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 同步发送
     */
    public boolean sync(String message){

        boolean flag = true;
        String text1 = "发送消息:" + message;
        SendResult sendResult1 = rocketMQTemplate.syncSend("business_topic:tag1",text1);
        if(!SendStatus.SEND_OK.equals(sendResult1.getSendStatus())){
            flag = false;
        }
        return  flag;

    }

}
