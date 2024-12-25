package com.business.businessdemo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
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

    /**
     * 创建索引
     */
    public void createIndex(){
        try {

            XContentBuilder mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("dynamic", true)

                    .startObject("properties")

                    .startObject("name")
                    .field("type", "text")
                    .startObject("fields")
                    .startObject("keyword")
                    .field("type", "keyword")
                    .endObject()
                    .endObject()
                    .endObject()

                    .startObject("address")
                    .field("type", "text")
                    .startObject("fields")
                    .startObject("keyword")
                    .field("type", "keyword")
                    .endObject()
                    .endObject()
                    .endObject()

                    .endObject()


                    .endObject();

            Settings settings = Settings.builder().put("index.number_of_shards", 5).put("index.number_of_replicas", 0).build();
            CreateIndexRequest extest = new CreateIndexRequest("extest", settings);
            extest.mapping("doc",mapping);

            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(extest, RequestOptions.DEFAULT);
            //判断是否创建成功
            boolean acknowledged = createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除索引
     */
    public void deleteIndex(){
        DeleteIndexRequest extest = new DeleteIndexRequest("extest");
        try {
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(extest, RequestOptions.DEFAULT);
            //是否删除成功
            boolean acknowledged = delete.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建文档
     * @return
     */
    public String addDocument(){
        IndexRequest doc = new IndexRequest("extest", "doc");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","张三");
        jsonObject.put("address","北京市");
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


    /**
     *
     * @param id
     */
    public void getDocument(String id){

        GetRequest doc = new GetRequest("extest", "doc", id);
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
            UpdateRequest updateRequest = new UpdateRequest("extest", "doc", id);
            // 设置员工更新信息
            JSONObject userInfo = new JSONObject();
            userInfo.put("name","200f");
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
            DeleteRequest deleteRequest = new DeleteRequest("extest", "doc", id);
            // 执行删除文档
            DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            log.info("删除状态：{}", response.status());
        } catch (IOException e) {
            log.error("", e);
        }
    }

    /**
     * 精确查询（查询条件不会进行分词，但是查询内容可能会分词，导致查询不到）
     */
    public void termQuery(){
        // 构建查询条件（注意：termQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("address.keyword","上海"));

        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("extest");
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().totalHits > 0){
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String sourceAsString = hit.getSourceAsString();
                    log.info("sourceAsString{}",sourceAsString);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多个内容在一个字段中进行查询
     */
    public void termsQuery(){
        try{
            // 构建查询条件（注意：termsQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.termsQuery("address.keyword", "北京市丰台区", "北京市昌平区", "北京市大兴区"));
            // 创建查询请求对象，将查询对象配置到其中
            SearchRequest searchRequest = new SearchRequest("extest");
            searchRequest.source(searchSourceBuilder);
            // 执行查询，然后处理响应结果
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 根据状态和数据条数验证是否返回了数据
            if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().totalHits > 0) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {

                }
            }
        }catch (IOException e) {
            log.error("", e);
        }
    }

    /**
     * 匹配查询符合条件的所有数据，并设置分页
     * @return
     */
    public Object matchAllQuery(){

        //构建查询条件
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQueryBuilder);

        // 设置分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(3);

        // 设置排序
        searchSourceBuilder.sort("address", SortOrder.ASC);

        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("extest");
        searchRequest.source(searchSourceBuilder);

        // 执行查询，然后处理响应结果
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 根据状态和数据条数验证是否返回了数据
            if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().totalHits > 0) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String sourceAsString = hit.getSourceAsString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 匹配查询数据--相当于模糊查询
     * @return
     */
    public Object matchQuery(){
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "*通州区"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest("extest");
        searchRequest.source(searchSourceBuilder);
        // 执行查询，然后处理响应结果
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().totalHits > 0) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
            }
        }

        return null;
    }

}
