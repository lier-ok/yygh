package com.lier.yygh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author lier
 * @date 2022/1/26 - 20:42
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class})//取消数据源自动配置
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceStatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceStatisticsApplication.class, args);
    }

}
