package com.lier.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.lier.yygh.enums.PaymentTypeEnum;
import com.lier.yygh.enums.RefundStatusEnum;
import com.lier.yygh.model.order.PaymentInfo;
import com.lier.yygh.model.order.RefundInfo;
import com.lier.yygh.service.OrderInfoService;
import com.lier.yygh.service.PaymentInfoService;
import com.lier.yygh.service.RefundInfoService;
import com.lier.yygh.service.WeiXinService;
import com.lier.yygh.utils.ConstantPropertiesUtils;
import com.lier.yygh.utils.HttpClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/24 - 17:47
 * @Decription
 * @since jdk1.8
 */
@Service
public class WeiXinServiceImpl implements WeiXinService {

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private RefundInfoService refundInfoService;


    @Override
    public Boolean refund(Long orderId) {
        try{
            PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            //判断退款状态
            if(RefundStatusEnum.REFUND.getStatus().intValue() == refundInfo.getRefundStatus().intValue()){//已退款
                return true;
            }
            Map<String,String> paramMap = new HashMap<>(8);
            paramMap.put("appid", ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
            //paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            //paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            //以下测试退款一分钱
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
