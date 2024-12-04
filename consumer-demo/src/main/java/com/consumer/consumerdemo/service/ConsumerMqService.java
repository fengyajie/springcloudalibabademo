package com.consumer.consumerdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "business_topic",consumerGroup = "consumer_mq_group")
public class ConsumerMqService implements RocketMQListener<MessageExt> {



    @Override
    public void onMessage(MessageExt messageExt) {

        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("接收到的消息{}",body);
        //抛出异常会重试
        //throw new RuntimeException();
    }
}
