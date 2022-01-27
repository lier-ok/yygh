package com.lier.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.model.order.PaymentInfo;
import com.lier.yygh.model.order.RefundInfo;

/**
 * @Author lier
 * @date 2022/1/24 - 17:32
 * @Decription
 * @since jdk1.8
 */
public interface RefundInfoService extends IService<RefundInfo> {

    //添加退款记录
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
