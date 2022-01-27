package com.lier.yygh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @Author lier
 * @date 2021/10/21 - 20:27
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.lier.yygh")
public class ServiceHospMainStarter {

    public static void main(String[] args) {
        SpringApplication.run(ServiceHospMainStarter.class,args);
    }
}
