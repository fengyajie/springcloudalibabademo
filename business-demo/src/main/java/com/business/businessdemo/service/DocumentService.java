package com.business.businessdemo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
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
import org.elasticsearch.index.query.Operator;
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
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DocumentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public static String index = "extest";

    public static String type = "doc";

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
                            /*.startObject("fields")
                                .startObject("keyword")
                                .field("type", "keyword")
                                .endObject()
                            .endObject()*/
                        .endObject()

                        .startObject("address")
                        .field("type", "text")
                        /*.startObject("fields")
                        .startObject("keyword")
                        .field("type", "keyword")
                        .endObject()
                        .endObject()*/
                        .endObject()

                        .startObject("birthday")
                          .field("type","date")
                          .field("format","yyyy-MM-dd")
                        .endObject()

                      .endObject()


                    .endObject();

            Settings settings = Settings.builder().put("number_of_shards", 5).put("number_of_replicas", 0).build();
            CreateIndexRequest extest = new CreateIndexRequest(index, settings);
            extest.mapping("doc",mapping);

            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(extest, RequestOptions.DEFAULT);
            //判断是否创建成功
            boolean acknowledged = createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查索引是否存在
     */
    public void exists() throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(index);

        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if(exists){

        }
    }

    /**
     * 删除索引
     */
    public void deleteIndex(){
        DeleteIndexRequest extest = new DeleteIndexRequest(index);
        try {
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(extest, RequestOptions.DEFAULT);
            //是否删除成功
            boolean acknowledged = delete.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建文档---手动指定id
     * @return
     */
    public String addDocument(String id){
        IndexRequest doc = new IndexRequest(index, type,id);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","张三");
        jsonObject.put("address","北京市");
        //设置文档内容
        doc.source(JSON.toJSONString(jsonObject), XContentType.JSON);
        try {
            IndexResponse indexResponse = restHighLevelClient.index(doc, RequestOptions.DEFAULT);
            DocWriteResponse.Result result = indexResponse.getResult();

            if(Objects.isNull(indexResponse) || indexResponse.status().getStatus() == 0){
                throw new RuntimeException("文档创建失败");
            }
            return indexResponse.getId();
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

        GetRequest doc = new GetRequest(index, type, id);
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
            UpdateRequest updateRequest = new UpdateRequest(index, type, id);
            // 设置员工更新信息
            JSONObject userInfo = new JSONObject();
            userInfo.put("name","200f");
            userInfo.put("address","北京市海淀区");
            // 设置更新文档内容
            updateRequest.doc(JSON.toJSONString(userInfo), XContentType.JSON);
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
            DeleteRequest deleteRequest = new DeleteRequest(index, type, id);
            // 执行删除文档
            DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            log.info("删除状态：{}", response.status());
        } catch (IOException e) {
            log.error("", e);
        }
    }


    /**
     * 批量创建
     */
    public void batchCreateDoc() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new IndexRequest(index,type,"").source("",XContentType.JSON));
        bulkRequest.add(new IndexRequest(index,type,"").source("",XContentType.JSON));

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

    }

    /**
     * 批量删除
     */
    public void batchDeleteDoc() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new DeleteRequest(index,type,""));
        bulkRequest.add(new DeleteRequest(index,type,""));

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

    }



    /**
     * 精确查询--完全匹配，不会对搜索的关键词进行分词，如果查询的text，text被分词就查不到，不是只能查keyword类型，
     * 相当于mysql where address =
     *
     */
    public void termQuery(){

        //创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);

        // 构建查询条件（注意：termQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //分页，可选
        //searchSourceBuilder.from(0);
        //searchSourceBuilder.size(5);
        searchSourceBuilder.query(QueryBuilders.termQuery("address","上海"));
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().totalHits > 0){
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String sourceAsString = hit.getSourceAsString();
                    log.info("sourceAsString{}",sourceAsString);
                    JSONObject.parseObject(sourceAsString);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多个内容在一个字段中进行查询
     * 相当于mysql where address in
     */
    public void termsQuery(){
        try{
            // 构建查询条件（注意：termsQuery 支持多种格式查询，如 boolean、int、double、string 等，这里使用的是 string 的查询）
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.termsQuery("address", "北京市丰台区", "北京市昌平区", "北京市大兴区"));
            // 创建查询请求对象，将查询对象配置到其中
            SearchRequest searchRequest = new SearchRequest(index);
            searchRequest.types(type);
            searchRequest.source(searchSourceBuilder);
            // 执行查询，然后处理响应结果
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 根据状态和数据条数验证是否返回了数据
            if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().totalHits > 0) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String sourceAsString = hit.getSourceAsString();
                }
            }
        }catch (IOException e) {
            log.error("", e);
        }
    }

    /**
     * match查询属于高层查询，他会根据你查询的字段类型不一样，采用不同的查询方式
     * 1，查询的是日期或者数值的话，他会将你基于字符串查询内容转换为日期或者数值对待。
     * 2，如果查询的内容是一个不能被分词的内容(keyword)，match查询不会对你指定的查询关键字进行分词
     * 3，如果查询的内容是一个可以被分词的内容(text),match会将你指定的查询内容根据一定的方式去分词
     *，去分词库中匹配指定的内容
     * match查询，实际顶层就是多个term查询，将多个term查询的结果封装一起
     */

    /**
     * 匹配查询符合条件的所有数据，不指定查询内容
     * es查询的内容多的话，默认10条
     * @return
     */
    public Object matchAllQuery(){

        //构建查询条件
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        // 创建查询源构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQueryBuilder);

        // 设置分页
        //searchSourceBuilder.from(0);
        //searchSourceBuilder.size(3);

        // 设置排序
        searchSourceBuilder.sort("address", SortOrder.ASC);

        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
     * 匹配查询数据,如果查询内容是text，查询关键字会被分词去分词库查询
     * @return
     */
    public Object matchQuery(){
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "通州区"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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

    /**
     * Operator.OR 匹配通州区或者 海淀区，AND匹配通州区和 海淀区
     */
    public void booleanMatch(){
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "通州区 海淀区").operator(Operator.OR));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }

    /**
     * 针对多个field做检索
     */
    public void multiMatch(){

        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("北京","address","name"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }

    /**
     * 根据id查询
     * @param id
     * @throws IOException
     */
    public void findById(String id) throws IOException {
        // 构建查询条件
        GetRequest getRequest = new GetRequest(index,type,id);

        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

        String sourceAsString = getResponse.getSourceAsString();
    }


    /**
     * 根据ids查询
     */
    public void findByIds(List<String> ids){
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.idsQuery().addIds(ids.get(0),ids.get(1)));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }

    /**
     * 前缀查询
     */
    public void findByPrefix(){

        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.prefixQuery("address","上海"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }

    /**
     * 模糊查询
     */
    public void fuzzyFind(){
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //模糊查询，前三个字不允许错别字
        searchSourceBuilder.query(QueryBuilders.fuzzyQuery("address","上海").prefixLength(2));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }

    /**
     * 通配查询
     */
    public void wildcardFind(){
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询到上海xx，使用*和?来实现通配和占位符
        searchSourceBuilder.query(QueryBuilders.wildcardQuery("address","上海??"));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }

    /**
     * 对某一字段进行数值范围查询
     */
    public void rangeFind(){
        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //gt:>,gte:>=,lt:<,lte:<=
        searchSourceBuilder.query(QueryBuilders.rangeQuery("age").gt(10));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }


    /**
     * 可以根据正则表达式查询
     */
    public void regexpFind(){

        // 构建查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //gt:>,gte:>=,lt:<,lte:<=
        searchSourceBuilder.query(QueryBuilders.regexpQuery("address",""));
        // 创建查询请求对象，将查询对象配置到其中
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
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
    }


    /**
     * ES对from+size分页是有限制的，两者之和不能超过1w
     */
    public void scrollFind(){

    }

}
