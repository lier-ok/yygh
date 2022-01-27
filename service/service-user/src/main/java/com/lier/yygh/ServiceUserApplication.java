package com.lier.yygh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author lier
 * @date 2021/11/19 - 22:03
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication(exclude= {MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class})
@EnableFeignClients("com.lier.yygh")
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class,args);
    }
}
