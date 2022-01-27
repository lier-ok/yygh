package com.lier.yygh.receiver;

import com.lier.yygh.constant.MqConst;
import com.lier.yygh.model.hosp.Schedule;
import com.lier.yygh.repository.ScheduleRepository;
import com.lier.yygh.service.RabbitService;
import com.lier.yygh.service.ScheduleService;
import com.lier.yygh.vo.msm.MsmVo;
import com.lier.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Author lier
 * @date 2022/1/19 - 16:29
 * @Decription
 * @since jdk1.8
 */
@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;


    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        //下单成功更新预约数
        if(null != orderMqVo.getAvailableNumber()){ //没有设置剩余预约数量即为提交订单
            Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            scheduleService.update(schedule);
        }else{//否则为取消已支付订单
            Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
            int availableNumber = schedule.getAvailableNumber() + 1;
            schedule.setReservedNumber(availableNumber);
            scheduleService.update(schedule);
        }

        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

}
