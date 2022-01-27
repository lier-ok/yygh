package com.lier.yygh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author lier
 * @date 2022/1/18 - 18:10
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.lier"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.lier"})
@EnableTransactionManagement//开启事务
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class,args);
    }
}
