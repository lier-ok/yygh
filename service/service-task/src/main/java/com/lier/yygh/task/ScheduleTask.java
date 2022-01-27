package com.lier.yygh.task;

import com.lier.yygh.constant.MqConst;
import com.lier.yygh.service.RabbitService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author lier
 * @date 2022/1/25 - 21:03
 * @Decription
 * @since jdk1.8
 */
@Component
@EnableScheduling
public class ScheduleTask {

    @Resource
    private RabbitService rabbitService;


    //真实情况 0 0 8 * * ? 每天8点执行
    //测试 30秒执行一次
    @Scheduled(cron = "0/30 * * * * ?")
    public void task() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }


}
