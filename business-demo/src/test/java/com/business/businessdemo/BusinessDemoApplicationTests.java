package com.business.businessdemo;

import com.business.businessdemo.service.BusnessMqSendService;
import com.business.businessdemo.service.DocumentService;
import com.business.businessdemo.service.IndexService;
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

    @Autowired
    private IndexService indexService;

    @Autowired
    private DocumentService documentService;

    @Test
    void contextLoads() {
    }

    @Test
    public void sendSyncMq(){
        busnessMqSendService.sync("测试消息");
    }

    @Test
    public void addIndex(){

        //indexService.createIndex();

        String id = documentService.addDocument();

        documentService.getDocument(id);

        documentService.updateDocument(id);

        documentService.getDocument(id);

    }

}
