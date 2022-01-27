package com.lier.yygh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * @Author lier
 * @date 2021/11/20 - 16:50
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class})
public class ServiceMsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceMsmApplication.class,args);
    }
}
