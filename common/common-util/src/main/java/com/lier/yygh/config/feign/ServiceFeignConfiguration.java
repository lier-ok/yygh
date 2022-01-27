package com.lier.yygh.config.feign;

import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Author lier
 * @date 2022/1/19 - 20:48
 * @Decription
 * @since jdk1.8
 */
@Component
public class ServiceFeignConfiguration {

    @Value("${service.feign.connectTimeout:60000}")
    private int connectTimeout;

    @Value("${service.feign.readTimeOut:60000}")
    private int readTimeout;

    @Bean
    public Request.Options options() {
        return new Request.Options(connectTimeout, readTimeout);
    }
}
