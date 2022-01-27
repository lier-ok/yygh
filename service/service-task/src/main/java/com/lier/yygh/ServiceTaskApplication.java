package com.lier.yygh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author lier
 * @date 2022/1/25 - 16:08
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class})//取消数据源自动配置
@EnableDiscoveryClient
public class ServiceTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceTaskApplication.class, args);
    }
}
