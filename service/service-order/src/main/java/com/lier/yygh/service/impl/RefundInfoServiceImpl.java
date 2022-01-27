package com.lier.yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lier.yygh.enums.PaymentTypeEnum;
import com.lier.yygh.enums.RefundStatusEnum;
import com.lier.yygh.mapper.RefundInfoMapper;
import com.lier.yygh.model.order.PaymentInfo;
import com.lier.yygh.model.order.RefundInfo;
import com.lier.yygh.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author lier
 * @date 2022/1/24 - 17:32
 * @Decription
 * @since jdk1.8
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",paymentInfo.getOrderId());
        wrapper.eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(wrapper);
        if(null != refundInfo){//已存在退款记录
            return refundInfo;
        }
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
