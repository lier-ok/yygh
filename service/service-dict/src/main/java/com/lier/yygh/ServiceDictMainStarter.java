package com.lier.yygh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author lier
 * @date 2021/10/21 - 20:27
 * @Decription
 * @since jdk1.8
 */
@SpringBootApplication
@ComponentScan("com.lier")
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceDictMainStarter {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDictMainStarter.class,args);
    }
}
