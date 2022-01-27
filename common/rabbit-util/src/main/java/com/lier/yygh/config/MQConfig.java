package com.lier.yygh.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Author lier
 * @date 2022/1/19 - 15:24
 * @Decription Mq消息转换器
 * @since jdk1.8
 */
@Component
public class MQConfig {

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
