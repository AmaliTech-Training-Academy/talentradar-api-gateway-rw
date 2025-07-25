package com.talentradar.apigatewayrw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableDiscoveryClient
public class ApiGatewayRwApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayRwApplication.class, args);
    }

}
