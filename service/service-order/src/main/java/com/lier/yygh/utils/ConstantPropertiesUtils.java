package com.lier.yygh.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author lier
 * @date 2022/1/22 - 15:13
 * @Decription
 * @since jdk1.8
 */
@Component
public class ConstantPropertiesUtils implements InitializingBean {

    @Value("${weixin.appid}")
    private String appId;

    @Value("${weixin.partner}")
    private String partner;

    @Value("${weixin.partnerkey}")
    private String partnerkey;

    @Value("${weixin.cert}")
    private String cert;


    public static String APPID;
    public static String PARTNER;
    public static String PARTNERKEY;
    public static String CERT;


    @Override
    public void afterPropertiesSet() throws Exception {
        APPID = this.appId;
        PARTNER = this.partner;
        PARTNERKEY = this.partnerkey;
        CERT = this.cert;
    }
}
