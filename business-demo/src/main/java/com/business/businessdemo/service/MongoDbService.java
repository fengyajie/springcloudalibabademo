package com.business.businessdemo.service;

import com.alibaba.fastjson.JSON;
import com.business.businessdemo.domain.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * mongodb操作
 */
@Slf4j
@Service
public class MongoDbService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /** 设置集合名称 */
    private static final String COLLECTION_NAME = "users";

    /**
     * 创建集合
     * 创建一个没有大小限制的集合
     * @return
     */
    public String createCollection(){

        String collectionName = "user1";
        MongoCollection<Document> collection = mongoTemplate.createCollection(collectionName);
        log.info("集合返回{}", JSON.toJSONString(collection));
        boolean b = mongoTemplate.collectionExists(collectionName);
        if(b){
            return "创建成功";
        }
        return "创建失败";
    }

    /**
     * 创建固定大小的集合
     * 创建集合并设置 `capped=true` 创建 `固定大小集合`，可以配置参数 `size` 限制文档大小，可以配置参数 `max` 限制集合文档数量
     * @return
     */
    public String createCollectionFixedSize(){

        // 设置集合名称
        String collectionName = "users2";
        // 设置集合参数
        long size = 1024L;
        long max = 5L;

        // 创建固定大小集合
        CollectionOptions collectionOptions = CollectionOptions.empty()
                // 创建固定集合。固定集合是指有着固定大小的集合，当达到最大值时，它会自动覆盖最早的文档。
                .capped()
                // 固定集合指定一个最大值，以千字节计(KB),如果 capped 为 true，也需要指定该字段。
                .size(size)
                // 指定固定集合中包含文档的最大数量。
                .maxDocuments(max);
        // 执行创建集合
        mongoTemplate.createCollection(collectionName, collectionOptions);
        if(mongoTemplate.collectionExists(collectionName)){
            return "创建成功";
        }
        return "创建失败";
    }

    /**
     * 创建【验证文档数据】的集合
     *
     * 创建集合并在文档"插入"与"更新"时进行数据效验，如果符合创建集合设置的条件就进允许更新与插入，否则则按照设置的设置的策略进行处理。
     *
     * * 效验级别：
     *   - off：关闭数据校验。
     *   - strict：(默认值) 对所有的文档"插入"与"更新"操作有效。
     *   - moderate：仅对"插入"和满足校验规则的"文档"做"更新"操作有效。对已存在的不符合校验规则的"文档"无效。
     * * 执行策略：
     *   - error：(默认值) 文档必须满足校验规则，才能被写入。
     *   - warn：对于"文档"不符合校验规则的 MongoDB 允许写入，但会记录一条告警到 mongod.log 中去。日志内容记录报错信息以及该"文档"的完整记录。
     *
     * @return 创建集合结果
     */
    public String createCollectionValidation(){
        // 设置集合名称
        String collectionName = "users3";
        // 设置验证条件,只允许岁数大于20的用户信息插入
        CriteriaDefinition criteria = Criteria.where("age").gt(20);
        // 设置集合选项验证对象
        CollectionOptions collectionOptions = CollectionOptions.empty()
                .validator(Validator.criteria(criteria))
                // 设置效验级别
                .strictValidation()
                // 设置效验不通过后执行的动作
                .failOnValidationError();
        // 执行创建集合
        mongoTemplate.createCollection(collectionName, collectionOptions);
        return mongoTemplate.collectionExists(collectionName)?"创建成功":"创建失败";
    }

    /**
     * 获取集合列表
     */
    public void getCollectionNames(){
        Set<String> collectionNames = mongoTemplate.getCollectionNames();
        log.info("集合列表{}",JSON.toJSONString(collectionNames));
    }

    /**
     * 删除集合
     */
    public void dropCollection(){
        // 设置集合名称
        String collectionName = "users3";
        // 执行删除集合
        mongoTemplate.getCollection(collectionName).drop();
    }

    /**
     * 创建视图
     */
    public String createView(){

        // 设置视图名
        String newViewName = "usersView";
        // 设置获取数据的集合名称
        String collectionName = "users";
        // 定义视图的管道,可是设置视图显示的内容多个筛选条件
        List<Bson> pipeline = new ArrayList<>();
        // 设置条件，用于筛选集合中的文档数据，只有符合条件的才会映射到视图中
        pipeline.add(Document.parse("{\"$match\":{\"sex\":\"女\"}}"));
        // 执行创建视图
        mongoTemplate.getDb().createView(newViewName, collectionName, pipeline);
        return mongoTemplate.collectionExists(newViewName)?"创建成功":"创建失败";
    }

    /**
     * 删除视图
     */
    public void dropView(){
        // 设置待删除的视图名称
        String viewName = "usersView";
        // 检测视图是否存在
        if (mongoTemplate.collectionExists(viewName)) {
            // 删除视图
            mongoTemplate.getDb().getCollection(viewName).drop();
        }
    }

    /**
     * 插入【一条】文档数据，如果文档信息已经【存在就抛出异常】
     */
    public void insert(){
        // 设置用户信息
        User user = new User()
                .setId("10")
                .setAge(22)
                .setSex("男")
                .setRemake("无")
                .setSalary(1500)
                .setName("zhangsan")
                .setBirthday(new Date());
        // 插入一条用户数据，如果文档信息已经存在就抛出异常
        User newUser = mongoTemplate.insert(user, COLLECTION_NAME);
        // 输出存储结果
        log.info("存储的用户信息为：{}", newUser);
    }

    /**
     * 插入【多条】文档数据，如果文档信息已经【存在就抛出异常】
     */
    public void insertMany(){
        // 设置两个用户信息
        User user1 = new User()
                .setId("11")
                .setAge(22)
                .setSex("男")
                .setRemake("无")
                .setSalary(1500)
                .setName("shiyi")
                .setBirthday(new Date());
        User user2 = new User()
                .setId("12")
                .setAge(22)
                .setSex("男")
                .setRemake("无")
                .setSalary(1500)
                .setName("shier")
                .setBirthday(new Date());
        // 使用户信息加入结合
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);
        // 插入一条用户数据，如果某个文档信息已经存在就抛出异常
        Collection<User> newUserList = mongoTemplate.insert(userList, COLLECTION_NAME);
    }

    /**
     *存储【一条】用户信息，如果文档信息已经【存在就执行更新】
     */
    public void save(){
        // 设置用户信息
        User user = new User()
                .setId("13")
                .setAge(22)
                .setSex("男")
                .setRemake("无")
                .setSalary(2800)
                .setName("kuiba")
                .setBirthday(new Date());
        // 存储用户信息,如果文档信息已经存在就执行更新
        User newUser = mongoTemplate.save(user, COLLECTION_NAME);
        // 输出存储结果
        log.info("存储的用户信息为：{}", newUser);
    }

    /**
     * 查询集合中的【全部】文档数据
     */
    public void findAll(){
        // 执行查询集合中全部文档信息
        List<User> documentList = mongoTemplate.findAll(User.class, COLLECTION_NAME);
        log.info("集合中的文档信息{}",JSON.toJSONString(documentList));
    }

    /**
     * 根据【文档ID】查询集合中文档数据
     */
    public void findById(){
        // 设置查询的文档 ID
        String id = "1";
        // 根据文档ID查询集合中文档数据，并转换为对应 Java 对象
        User user = mongoTemplate.findById(id, User.class, COLLECTION_NAME);
        // 输出结果
        log.info("用户信息：{}", user);
    }

    /**
     * 根据【条件】查询集合中【符合条件】的文档，只取【第一条】数据
     *
     * @return 符合条件的第一条文档
     */
    public Object findOne() {
        // 设置查询条件参数
        int age = 22;
        // 创建条件对象
        Criteria criteria = Criteria.where("age").is(age);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询一条文档，如果查询结果中有多条文档，那么就取第一条
        User user = mongoTemplate.findOne(query, User.class, COLLECTION_NAME);
        // 输出结果
        log.info("用户信息：{}", user);
        return user;
    }

    /**
     * 根据【条件】查询集合中【符合条件】的文档，获取其【文档列表】
     *
     * @return 符合条件的文档列表
     */
    public Object findByCondition() {
        // 设置查询条件参数
        String sex = "女";
        // 创建条件对象
        Criteria criteria = Criteria.where("sex").is(sex);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 根据【条件】查询集合中【符合条件】的文档，获取其【文档列表】并【排序】
     *
     * @return 符合条件的文档列表
     */
    public Object findByConditionAndSort() {
        // 设置查询条件参数
        String sex = "男";
        String sort = "age";
        // 创建条件对象
        Criteria criteria = Criteria.where("sex").is(sex);
        // 创建查询对象，然后将条件对象添加到其中，然后根据指定字段进行排序
        Query query = new Query(criteria).with(Sort.by(sort));
        // 执行查询
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 根据【单个条件】查询集合中的文档数据，并【按指定字段进行排序】与【限制指定数目】
     *
     * @return 符合条件的文档列表
     */
    public Object findByConditionAndSortLimit() {
        // 设置查询条件参数
        String sex = "男";
        String sort = "age";
        int limit = 2;
        // 创建条件对象
        Criteria criteria = Criteria.where("sex").is(sex);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria).with(Sort.by(sort)).limit(limit);
        // 执行查询
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 根据【单个条件】查询集合中的文档数据，并【按指定字段进行排序】与【并跳过指定数目】
     *
     * @return 符合条件的文档列表
     */
    public Object findByConditionAndSortSkip() {
        // 设置查询条件参数
        String sex = "男";
        String sort = "age";
        int skip = 1;
        // 创建条件对象
        Criteria criteria = Criteria.where("sex").is(sex);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria).with(Sort.by(sort)).skip(skip);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 查询【存在指定字段名称】的文档数据
     *
     * @return 符合条件的文档列表
     */
    public Object findByExistsField() {
        // 设置查询条件参数
        String field = "sex";
        // 创建条件
        Criteria criteria = Criteria.where(field).exists(true);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 根据【AND】关联多个查询条件，查询集合中的文档数据
     *
     * @return 符合条件的文档列表
     */
    public Object findByAndCondition() {
        // 设置查询条件参数
        String sex = "男";
        Integer age = 22;
        // 创建条件
        Criteria criteriaSex = Criteria.where("sex").is(sex);
        Criteria criteriaAge = Criteria.where("age").is(age);
        // 创建条件对象，将上面条件进行 AND 关联
        Criteria criteria = new Criteria().andOperator(criteriaSex, criteriaAge);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 根据【OR】关联多个查询条件，查询集合中的文档数据
     *
     * @return 符合条件的文档列表
     */
    public Object findByOrCondition() {
        // 设置查询条件参数
        String sex = "男";
        int age = 22;
        // 创建条件
        Criteria criteriaSex = Criteria.where("sex").is(sex);
        Criteria criteriaAge = Criteria.where("age").is(age);
        // 创建条件对象，将上面条件进行 OR 关联
        Criteria criteria = new Criteria().orOperator(criteriaSex, criteriaAge);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }


    /**
     * 根据【IN】关联多个查询条件，查询集合中的文档数据
     *
     * @return 符合条件的文档列表
     */
    public Object findByInCondition() {
        // 设置查询条件参数
        Integer[] ages = {20, 22, 25};
        // 创建条件
        List<Integer> ageList = Arrays.asList(ages);
        // 创建条件对象
        Criteria criteria = Criteria.where("age").in(ageList);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 根据【逻辑运算符】查询集合中的文档数据
     *
     * @return 符合条件的文档列表
     */
    public Object findByOperator() {
        // 设置查询条件参数
        int min = 25;
        int max = 35;
        // 创建条件对象
        Criteria criteria = Criteria.where("age").gt(min).lte(max);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 根据【正则表达式】查询集合中的文档数据
     *
     * @return 符合条件的文档列表
     */
    public Object findByRegex() {
        // 设置查询条件参数
        String regex = "^zh*";
        // 创建条件对象
        Criteria criteria = Criteria.where("name").regex(regex);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        List<User> documentList = mongoTemplate.find(query, User.class, COLLECTION_NAME);
        // 输出结果
        for (User user : documentList) {
            log.info("用户信息：{}", user);
        }
        return documentList;
    }

    /**
     * 统计集合中符合【查询条件】的文档【数量】
     *
     * @return 符合条件的文档列表
     */
    public Object countNumber() {
        // 设置查询条件参数
        int age = 22;
        // 创建条件对象
        Criteria criteria = Criteria.where("age").is(age);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 查询并返回结果
        long count = mongoTemplate.count(query, User.class, COLLECTION_NAME);
        // 输出结果
        log.info("符合条件的文档数量：{}", count);
        return count;
    }

    /**
     * 更新集合中【匹配】查询到的第一条文档数据，如果没有找到就【创建并插入一个新文档】
     *
     * @return 执行更新的结果
     */
    public Object update() {
        // 创建条件对象
        Criteria criteria = Criteria.where("age").is(30);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 创建更新对象,并设置更新的内容
        Update update = new Update().set("age", 33).set("name", "zhangsansan");
        // 执行更新，如果没有找到匹配查询的文档，则创建并插入一个新文档
        UpdateResult result = mongoTemplate.upsert(query, update, User.class, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "匹配到" + result.getMatchedCount() + "条数据,对第一条数据进行了更改";
        log.info("更新结果：{}", resultInfo);
        return resultInfo;
    }

    /**
     * 更新集合中【匹配】查询到的【文档数据集合】中的【第一条数据】
     *
     * @return 执行更新的结果
     */
    public Object updateFirst() {
        // 创建条件对象
        Criteria criteria = Criteria.where("name").is("zhangsan");
        // 创建查询对象，然后将条件对象添加到其中，并设置排序
        Query query = new Query(criteria).with(Sort.by("age").ascending());
        // 创建更新对象,并设置更新的内容
        Update update = new Update().set("age", 30).set("name", "zhangsansan");
        // 执行更新
        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "共匹配到" + result.getMatchedCount() + "条数据,修改了" + result.getModifiedCount() + "条数据";
        log.info("更新结果：{}", resultInfo);
        return resultInfo;
    }

    /**
     * 更新【匹配查询】到的【文档数据集合】中的【所有数据】
     *
     * @return 执行更新的结果
     */
    public Object updateMany() {
        // 创建条件对象
        Criteria criteria = Criteria.where("age").gt(28);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 设置更新字段和更新的内容
        Update update = new Update().set("age", 29).set("salary", "1999");
        // 执行更新
        UpdateResult result = mongoTemplate.updateMulti(query, update, User.class, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "总共匹配到" + result.getMatchedCount() + "条数据,修改了" + result.getModifiedCount() + "条数据";
        log.info("更新结果：{}", resultInfo);
        return resultInfo;
    }

    /**
     * 删除集合中【符合条件】的【一个]或[多个】文档
     *
     * @return 删除用户信息的结果
     */
    public Object remove() {
        // 设置查询条件参数
        int age = 30;
        String sex = "男";
        // 创建条件对象
        Criteria criteria = Criteria.where("age").is(age).and("sex").is(sex);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 执行删除查找到的匹配的全部文档信息
        DeleteResult result = mongoTemplate.remove(query, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "成功删除 " + result.getDeletedCount() + " 条文档信息";
        log.info(resultInfo);
        return resultInfo;
    }

    /**
     * 删除【符合条件】的【单个文档】，并返回删除的文档。
     *
     * @return 删除的用户信息
     */
    public Object findAndRemove() {
        // 设置查询条件参数
        String name = "zhangsansan";
        // 创建条件对象
        Criteria criteria = Criteria.where("name").is(name);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 执行删除查找到的匹配的第一条文档,并返回删除的文档信息
        User result = mongoTemplate.findAndRemove(query, User.class, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "成功删除文档信息，文档内容为：" + result;
        log.info(resultInfo);
        return result;
    }

    /**
     * 删除【符合条件】的【全部文档】，并返回删除的文档。
     *
     * @return 删除的全部用户信息
     */
    public Object findAllAndRemove() {
        // 设置查询条件参数
        int age = 22;
        // 创建条件对象
        Criteria criteria = Criteria.where("age").is(age);
        // 创建查询对象，然后将条件对象添加到其中
        Query query = new Query(criteria);
        // 执行删除查找到的匹配的全部文档,并返回删除的全部文档信息
        List<User> resultList = mongoTemplate.findAllAndRemove(query, User.class, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "成功删除文档信息，文档内容为：" + resultList;
        log.info(resultInfo);
        return resultList;
    }

    /**
     * 使用管道操作符 $group 结合 $count 方法进行聚合统计
     *
     * @return 聚合结果
     */
    public Object aggregationGroupCount() {
        // 使用管道操作符 $group 进行分组，然后统计各个组的文档数量
        AggregationOperation group = Aggregation.group("age").count().as("numCount");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用管道操作符 $group 结合表达式操作符 $max 进行聚合统计
     *
     * @return 聚合结果
     */
    public Object aggregationGroupMax() {
        // 使用管道操作符 $group 进行分组，然后统计各个组文档某字段最大值
        AggregationOperation group = Aggregation.group("sex").max("salary").as("salaryMax");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用管道操作符 $group 结合表达式操作符 $min 进行聚合统计
     *
     * @return 聚合结果
     */
    public Object aggregationGroupMin() {
        // 使用管道操作符 $group 进行分组，然后统计各个组文档某字段最小值
        AggregationOperation group = Aggregation.group("sex").min("salary").as("salaryMin");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用管道操作符 $group 结合表达式操作符 $sum 进行聚合统计
     *
     * @return 聚合结果
     */
    public Object aggregationGroupSum() {
        // 使用管道操作符 $group 进行分组，然后统计各个组文档某字段值合计
        AggregationOperation group = Aggregation.group("sex").sum("salary").as("salarySum");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用管道操作符 $group 结合表达式操作符 $avg 进行聚合统计
     *
     * @return 聚合结果
     */
    public Object aggregationGroupAvg() {
        // 使用管道操作符 $group 进行分组，然后统计各个组文档某字段值平均值
        AggregationOperation group = Aggregation.group("sex").avg("salary").as("salaryAvg");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用管道操作符 $group 结合表达式操作符 $first 获取每个组的包含某字段的文档的第一条数据
     *
     * @return 聚合结果
     */
    public Object aggregationGroupFirst() {
        // 先对数据进行排序，然后使用管道操作符 $group 进行分组，最后统计各个组文档某字段值第一个值
        AggregationOperation sort = Aggregation.sort(Sort.by("salary").ascending());
        AggregationOperation group = Aggregation.group("sex").first("salary").as("salaryFirst");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(sort, group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用管道操作符 $group 结合表达式操作符 $last 获取每个组的包含某字段的文档的最后一条数据
     *
     * @return 聚合结果
     */
    public Object aggregationGroupLast() {
        // 先对数据进行排序，然后使用管道操作符 $group 进行分组，最后统计各个组文档某字段值第最后一个值
        AggregationOperation sort = Aggregation.sort(Sort.by("salary").ascending());
        AggregationOperation group = Aggregation.group("sex").last("salary").as("salaryLast");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(sort, group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用管道操作符 $group 结合表达式操作符 $push 获取某字段列表
     *
     * @return 聚合结果
     */
    public Object aggregationGroupPush() {
        // 先对数据进行排序，然后使用管道操作符 $group 进行分组，然后以数组形式列出某字段的全部值
        AggregationOperation push = Aggregation.group("sex").push("salary").as("salaryFirst");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(push);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用 $group 和 $match 聚合,先使用 $match 过滤文档，然后再使用 $group 进行分组
     *
     * @return 聚合结果
     */
    public Object aggregateGroupMatch() {
        // 设置聚合条件，先使用 $match 过滤岁数大于 25 的用户，然后按性别分组，统计每组用户工资最高值
        AggregationOperation match = Aggregation.match(Criteria.where("age").lt(25));
        AggregationOperation group = Aggregation.group("sex").max("salary").as("sexSalary");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(match, group);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用 $group 和 $sort 聚合,先使用 $group 进行分组，然后再使用 $sort 排序
     *
     * @return 聚合结果
     */
    public Object aggregateGroupSort() {
        // 设置聚合条件，按岁数分组，然后统计每组用户工资最大值和用户数，按每组用户工资最大值升序排序
        AggregationOperation group = Aggregation.group("age")
                .max("salary").as("ageSalary")
                .count().as("ageCount");
        AggregationOperation sort = Aggregation.sort(Sort.by("ageSalary").ascending());
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group, sort);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用 $group 和 $limit 聚合,先使用 $group 进行分组，然后再使用 $limit 限制一定数目文档
     *
     * @return 聚合结果
     */
    public Object aggregateGroupLimit() {
        // 设置聚合条件，先按岁数分组，然后求每组用户的工资总数、最大值、最小值、平均值，限制只能显示五条
        AggregationOperation group = Aggregation.group("age")
                .sum("salary").as("sumSalary")
                .max("salary").as("maxSalary")
                .min("salary").as("minSalary")
                .avg("salary").as("avgSalary");
        AggregationOperation limit = Aggregation.limit(5L);
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group, limit);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用 $group 和 $skip 聚合,先使用 $group 进行分组，然后再使用 $skip 跳过一定数目文档
     *
     * @return 聚合结果
     */
    public Object aggregateGroupSkip() {
        // 设置聚合条件，先按岁数分组，然后求每组用户的工资总数、最大值、最小值、平均值，跳过前 2 条
        AggregationOperation group = Aggregation.group("age")
                .sum("salary").as("sumSalary")
                .max("salary").as("maxSalary")
                .min("salary").as("minSalary")
                .avg("salary").as("avgSalary");
        AggregationOperation limit = Aggregation.skip(2L);
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group, limit);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用 $group 和 $project 聚合,先使用 $group 进行分组，然后再使用 $project 限制显示的字段
     *
     * @return 聚合结果
     */
    public Object aggregateGroupProject() {
        // 设置聚合条件,按岁数分组，然后求每组用户工资最大值、最小值，然后使用 $project 限制值显示 salaryMax 字段
        AggregationOperation group = Aggregation.group("age")
                .max("salary").as("maxSalary")
                .min("salary").as("minSalary");
        AggregationOperation project = Aggregation.project("maxSalary");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(group, project);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }

    /**
     * 使用 $group 和 $unwind 聚合,先使用 $project 进行分组，然后再使用 $unwind 拆分文档中的数组为一条新文档记录
     *
     * @return 聚合结果
     */
    public Object aggregateProjectUnwind() {
        // 设置聚合条件，设置显示`name`、`age`、`title`字段，然后将结果中的多条文档按 title 字段进行拆分
        AggregationOperation project = Aggregation.project("name", "age", "title");
        AggregationOperation unwind = Aggregation.unwind("title");
        // 将操作加入到聚合对象中
        Aggregation aggregation = Aggregation.newAggregation(project, unwind);
        // 执行聚合查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Map.class);
        for (Map result : results.getMappedResults()) {
            log.info("{}", result);
        }
        return results.getMappedResults();
    }



}
