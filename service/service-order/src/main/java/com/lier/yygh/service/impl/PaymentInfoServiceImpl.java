package com.lier.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayUtil;
import com.lier.yygh.HospFeignClient;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.enums.OrderStatusEnum;
import com.lier.yygh.enums.PaymentStatusEnum;
import com.lier.yygh.enums.PaymentTypeEnum;
import com.lier.yygh.helper.HttpRequestHelper;
import com.lier.yygh.mapper.PaymentInfoMapper;
import com.lier.yygh.model.order.OrderInfo;
import com.lier.yygh.model.order.PaymentInfo;
import com.lier.yygh.service.OrderInfoService;
import com.lier.yygh.service.PaymentInfoService;
import com.lier.yygh.utils.ConstantPropertiesUtils;
import com.lier.yygh.utils.HttpClient;
import com.lier.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/22 - 15:34
 * @Decription
 * @since jdk1.8
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
        implements PaymentInfoService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private HospFeignClient hospFeignClient;


    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",orderInfo.getOutTradeNo());
        wrapper.eq("payment_type",paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0){//已有该支付记录
            return;
        }
        //像支付记录表中插入数据
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate())
                .toString("yyyy-MM-dd")+"|"+orderInfo
                .getHosname()+"|"+orderInfo
                .getDepname()+"|"+orderInfo.getTitle();

        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
    }

    @Override
    public Map<String, String> getPaymentStatus(Long orderId, String name) {

        try{
            //获取订单信息
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            //封装请求参数
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put("appid", ConstantPropertiesUtils.APPID);
            requestMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            requestMap.put("out_trade_no",orderInfo.getOutTradeNo());
            requestMap.put("nonce_str", WXPayUtil.generateNonceStr());

            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            //client设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(requestMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();//发送请求

            //处理返回结果
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            System.out.println(resultMap);
            return resultMap;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resMap) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",out_trade_no);
        wrapper.eq("payment_type",PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);

        //更新支付记录表
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(resMap.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(resMap.toString());
        this.updatePaymentInfo(out_trade_no, paymentInfo);


        //更新订单表
        OrderInfo orderInfo = orderInfoService.getOrderDetail(Long.parseLong(paymentInfo.getOutTradeNo()));
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);

        //更新医院的支付状态
        SignInfoVo signInfoVo = hospFeignClient.getSignKey(orderInfo.getHoscode());
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        BigInteger bigInteger = new BigInteger(orderInfo.getScheduleId(),16);
        BigInteger res = new BigInteger("29919475834915000000000000000");
        long id = bigInteger.subtract(res).longValue();
        paramMap.put("scheduleId",id);
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String signKey = HttpRequestHelper.getSign(paramMap,signInfoVo.getSignKey());
        paramMap.put("sign",signKey);
        System.out.println(signKey);
        System.out.println(signInfoVo.getApiUrl());
        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");

        if(jsonObject.getInteger("code") != 200) {
            throw new YyghException(jsonObject.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",orderId);
        wrapper.eq("payment_type",paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        return paymentInfo;
    }

    private void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",out_trade_no);
        baseMapper.update(paymentInfo,wrapper);

    }
}
