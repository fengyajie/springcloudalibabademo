package com.gateway.gatewaydemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway.routes.config")
@RefreshScope
@Data
public class GatewayRouteConfigProperties {
    private long DEFAULT_TIMEOUT = 30000;
    private String dataId;
    private String group;
    private String namespace;

    private String serviceAddr;
}
