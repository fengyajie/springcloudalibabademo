package com.gateway.gatewaydemo.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.gatewaydemo.service.DynamicRouteServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 动态路由方式1
 */
@Slf4j
@Component
@DependsOn("gatewayRouteConfigProperties")
public class GatewayRouteInitConfig {

    @Autowired
    private GatewayRouteConfigProperties gatewayRouteConfigProperties;

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    @Autowired
    private DynamicRouteServiceImpl routeService;

    /**
     * nacos 配置服务
     */
    private ConfigService configService;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("开始网关动态路由初始化...");
        initConfigService();

        try {
            // getConfigAndSignListener()方法 发起长轮询和对dataId数据变更注册监听的操作
            // getConfig 只是发送普通的HTTP请求
            String initConfigInfo = configService.getConfigAndSignListener(gatewayRouteConfigProperties.getDataId(), gatewayRouteConfigProperties.getGroup(), nacosConfigProperties.getTimeout(), new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    if (StringUtils.isNotEmpty(configInfo)) {
                        log.info("接收到网关路由更新配置：{}", configInfo);
                        List<RouteDefinition> routeDefinitions = null;
                        try {
                            routeDefinitions = objectMapper.readValue(configInfo, new TypeReference<List<RouteDefinition>>() {
                            });
                        } catch (JsonProcessingException e) {
                            log.error("解析路由配置出错，" + e.getMessage(), e);
                        }
                        for (RouteDefinition definition : Objects.requireNonNull(routeDefinitions)) {
                            routeService.updateById(definition);
                        }

                    } else {

                        log.warn("当前网关无动态路由相关配置");

                    }

                }

            });

            log.info("获取网关当前动态路由配置:\r\n{}", initConfigInfo);
            if (StringUtils.isNotEmpty(initConfigInfo)) {
                List<RouteDefinition> routeDefinitions = objectMapper.readValue(initConfigInfo, new TypeReference<List<RouteDefinition>>() {

                });

                for (RouteDefinition definition : routeDefinitions) {
                    routeService.add(definition);
                }

            } else {
                log.warn("当前网关无动态路由相关配置");
            }
            log.info("结束网关动态路由初始化...");
        } catch (Exception e) {

            log.error("初始化网关路由时发生错误", e);
        }
    }

    /**
     * 初始化网关路由 nacos config
     *
     * @return
     */
    private ConfigService initConfigService() {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", gatewayRouteConfigProperties.getServiceAddr());
            properties.setProperty("namespace", gatewayRouteConfigProperties.getNamespace());
            return configService = NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            log.error("初始化网关路由时发生错误", e);
            return null;
        }
    }
}
