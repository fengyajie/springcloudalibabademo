package com.business.businessdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 创建索引
 */
@Slf4j
@Service
public class IndexService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void createIndex(){
        try {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("dynamic",true)
                    .startObject("properties")

                    .startObject("name")
                    .field("type","text")
                    .startObject("fields")
                    .startObject("keyword")
                    .field("type","keyword")
                    .endObject()
                    .endObject()
                    .endObject()

                    .startObject("address")
                    .field("type","text")
                    .startObject("fields")
                    .startObject("keyword")
                    .field("type","keyword")
                    .endObject()
                    .endObject()
                    .endObject()

                    .startObject("remark")
                    .field("type","text")
                    .startObject("fields")
                    .startObject("keyword")
                    .field("type","keyword")
                    .endObject()
                    .endObject()
                    .endObject()

                    .startObject("age")
                    .field("type","integer")
                    .endObject()

                    .startObject("salary")
                    .field("type","float")
                    .endObject()

                    .startObject("birthDate")
                    .field("type","date")
                    .field("format", "yyyy-MM-dd")
                    .endObject()

                    .startObject("createTime")
                    .field("type","date")
                    .endObject()

                    .endObject()
                    .endObject();

            Settings setting = Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 0).build();
            CreateIndexRequest request = new CreateIndexRequest("mydlq-user", setting);
            request.mapping("doc", xContentBuilder);
            //执行创建索引
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            //是否创建成功
            boolean acknowledged = createIndexResponse.isAcknowledged();
            if(!acknowledged){
                throw new RuntimeException("创建失败");
            }
        } catch (IOException e) {
            log.error("创建失败{}",e);
        }
    }

    //删除索引
    public void deleteIndex(){

        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("mydlq-user");
        try {
            AcknowledgedResponse isDelete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            if(isDelete.isAcknowledged()){
                throw new RuntimeException("删除索引失败");
            }
        } catch (IOException e) {
            log.error("删除索引失败{}",e);
        }

    }

}
