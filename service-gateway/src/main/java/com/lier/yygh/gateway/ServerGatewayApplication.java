package com.lier.yygh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author lier
 * @date 2021/11/15 - 17:08
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServerGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerGatewayApplication.class,args);
    }
}
