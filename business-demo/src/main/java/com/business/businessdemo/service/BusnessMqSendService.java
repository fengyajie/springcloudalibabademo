package com.business.businessdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
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
        /*SendResult sendResult1 = rocketMQTemplate.syncSend("business_topic:tag1",text1);
        if(!SendStatus.SEND_OK.equals(sendResult1.getSendStatus())){
            flag = false;
        }*/

        //顺序发送
       /* SendResult consumer = rocketMQTemplate.syncSendOrderly("business_topic:tag1", text1, "consumer");
        SendResult consumer1 = rocketMQTemplate.syncSendOrderly("business_topic:tag1", "发送消息1", "consumer");
        SendResult consumer2 = rocketMQTemplate.syncSendOrderly("business_topic:tag1", "发送消息2", "consumer");
        SendResult consumer3 = rocketMQTemplate.syncSendOrderly("business_topic:tag1", "发送消息3", "consumer");
        SendResult consumer4 = rocketMQTemplate.syncSendOrderly("business_topic:tag1", "发送消息4", "consumer");
        if(!SendStatus.SEND_OK.equals(consumer.getSendStatus())){
            flag = false;
        }*/


        //延迟消息
        //SendResult sendResult = rocketMQTemplate.syncSend("business_topic:tag1", MessageBuilder.withPayload("延迟消息").build(), 3000, 4);

        rocketMQTemplate.asyncSend("business_topic:tag1", MessageBuilder.withPayload("异步消息").build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("异步消息发送成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("异步消息发送失败");
            }
        });

        return  flag;

    }

}
