package com.lier.yygh.util;


import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author lier
 * @date 2021/12/3 - 17:56
 * @Decription 读取配置文件工具类
 * @since jdk1.8
 */
@Component
public class ConstantOssProperties implements InitializingBean {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.secret}")
    private String secret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    public static String ENDPOINT;
    public static String ACCESS_KEY_ID;
    public static String SECRET;
    public static String BUCKET;



    @Override
    public void afterPropertiesSet() throws Exception {
        ENDPOINT = endpoint;
        ACCESS_KEY_ID = accessKeyId;
        SECRET = secret;
        BUCKET = bucket;
    }
}
