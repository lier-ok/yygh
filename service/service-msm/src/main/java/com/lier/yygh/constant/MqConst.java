package com.lier.yygh.constant;

/**
 * @Author lier
 * @date 2022/1/19 - 15:27
 * @Decription mq的常用配置
 * @since jdk1.8
 */
public class MqConst {

    //订单
    public static final String EXCHANGE_DIRECT_ORDER
            = "exchange.direct.order";
    public static final String ROUTING_ORDER = "order";
    //队列
    public static final String QUEUE_ORDER  = "queue.order";
    /**
     * 短信
     */
    public static final String EXCHANGE_DIRECT_MSM = "exchange.direct.msm";
    public static final String ROUTING_MSM_ITEM = "msm.item";
    //队列
    public static final String QUEUE_MSM_ITEM  = "queue.msm.item";

}
