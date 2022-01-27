package com.lier.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.enums.PaymentTypeEnum;
import com.lier.yygh.model.order.OrderInfo;
import com.lier.yygh.model.order.PaymentInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/22 - 15:30
 * @Decription
 * @since jdk1.8
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

    Map<String, String> getPaymentStatus(Long orderId, String name);

    @Transactional
    void paySuccess(String out_trade_no, Map<String, String> resMap);

    PaymentInfo getPaymentInfo(Long orderId,Integer paymentType);
}
