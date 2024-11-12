package com.consumer.consumerdemo;


import com.common.api.RemoteBusinessApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private RemoteBusinessApi remoteBusinessApi;

   @GetMapping("/getConsumer")
   public void getConsumer(){
       String test = remoteBusinessApi.test();
       System.out.println(test);
   }
}
