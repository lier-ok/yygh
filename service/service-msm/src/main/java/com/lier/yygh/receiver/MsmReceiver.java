package com.lier.yygh.receiver;

import com.lier.yygh.constant.MqConst;
import com.lier.yygh.service.MsmService;
import com.lier.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author lier
 * @date 2022/1/19 - 16:23
 * @Decription
 * @since jdk1.8
 */
@Component
public class MsmReceiver {

    @Resource
    private MsmService msmService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo, Message message, Channel channel) {
        msmService.send(msmVo);
    }

}
