package com.business.businessdemo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
public class DocumentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public String addDocument(){
        IndexRequest doc = new IndexRequest("mydlq-user", "doc");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","张三");
        jsonObject.put("age","33");
        jsonObject.put("salary","100.00f");
        jsonObject.put("address","北京市");
        jsonObject.put("remark","备注");
        jsonObject.put("createTime", LocalDateTime.now());
        jsonObject.put("birthDate","1990-01-01");
        byte[] bytes = JSONObject.toJSONBytes(jsonObject);
        //设置文档内容
        doc.source(bytes, XContentType.JSON);
        try {
            IndexResponse index = restHighLevelClient.index(doc, RequestOptions.DEFAULT);
            if(Objects.isNull(index) || index.status().getStatus() == 0){
                throw new RuntimeException("文档创建失败");
            }
            return index.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void getDocument(String id){

        GetRequest doc = new GetRequest("mydlq-user", "doc", id);
        try {
            GetResponse documentFields = restHighLevelClient.get(doc, RequestOptions.DEFAULT);
            if(documentFields.isExists()){
                Object parse = JSONObject.parse(documentFields.getSourceAsBytes());
                String s = JSON.toJSONString(parse);
                log.info("获取文档{}",s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 更新文档信息
     */
    public void updateDocument(String id) {
        try {
            // 创建索引请求对象
            UpdateRequest updateRequest = new UpdateRequest("mydlq-user", "doc", id);
            // 设置员工更新信息
            JSONObject userInfo = new JSONObject();
            userInfo.put("salary","200f");
            userInfo.put("address","北京市海淀区");
            // 将对象转换为 byte 数组
            byte[] json = JSON.toJSONBytes(userInfo);
            // 设置更新文档内容
            updateRequest.doc(json, XContentType.JSON);
            // 执行更新文档
            UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("创建状态：{}", response.status());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 删除文档信息
     */
    public void deleteDocument(String id) {
        try {
            // 创建删除请求对象
            DeleteRequest deleteRequest = new DeleteRequest("mydlq-user", "doc", id);
            // 执行删除文档
            DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            log.info("删除状态：{}", response.status());
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
