package com.gateway.gatewaydemo.service;

import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 动态路由方式二
 */
@Component
public class DynamicRouteService {

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;

    public DynamicRouteService(RouteDefinitionWriter routeDefinitionWriter,
                               RouteDefinitionLocator routeDefinitionLocator) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @EventListener
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        if (event.getKeys().contains("spring.cloud.gateway.routes")) {
            routeDefinitionLocator.getRouteDefinitions()
                    .subscribe(routeDefinitions -> {
                        routeDefinitionWriter.delete(Mono.just("*")).subscribe();
                        routeDefinitionWriter.save(Mono.just(routeDefinitions)).subscribe();
                    });
        }
    }
}
