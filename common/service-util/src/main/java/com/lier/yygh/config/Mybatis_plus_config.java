package com.lier.yygh.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author lier
 * @date 2021/10/23 - 21:24
 * @Decription
 * @since jdk1.8
 */
@Configuration
public class Mybatis_plus_config {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor page = new PaginationInterceptor();
        return page;
    }
}
