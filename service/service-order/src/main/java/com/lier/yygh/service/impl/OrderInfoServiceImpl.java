package com.lier.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayUtil;
import com.lier.yygh.HospFeignClient;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.constant.MqConst;
import com.lier.yygh.enums.OrderStatusEnum;
import com.lier.yygh.enums.PaymentTypeEnum;
import com.lier.yygh.helper.HttpRequestHelper;
import com.lier.yygh.mapper.OrderInfoMapper;
import com.lier.yygh.model.order.OrderInfo;
import com.lier.yygh.model.user.Patient;
import com.lier.yygh.service.*;
import com.lier.yygh.user.UserFeignClient;
import com.lier.yygh.utils.ConstantPropertiesUtils;
import com.lier.yygh.utils.HttpClient;
import com.lier.yygh.vo.hosp.ScheduleOrderVo;
import com.lier.yygh.vo.msm.MsmVo;
import com.lier.yygh.vo.order.*;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author lier
 * @date 2022/1/18 - 18:20
 * @Decription
 * @since jdk1.8
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private HospFeignClient hospFeignClient;

    @Resource
    private RabbitService rabbitService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RefundInfoService refundInfoService;

    @Resource
    private WeiXinService weiXinService;

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //?????????????????????
        Patient patient = userFeignClient.getPatient(patientId);
        if(patient == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //??????????????????
        ScheduleOrderVo scheduleOrderVo = hospFeignClient.getOrderData(scheduleId);
        if(scheduleOrderVo == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //????????????????????????????????????
//        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
//                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()){
//            throw new YyghException(ResultCodeEnum.NUMBER_NO);
//        }
        //??????????????????
        SignInfoVo signInfoVo = hospFeignClient.getSignKey(scheduleOrderVo.getHoscode());
        if(scheduleOrderVo.getAvailableNumber() <= 0) {
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        //???scheduleOrderVo??????????????????orderInfo???
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
        //????????????
        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //?????????
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
         JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl()+"/order/submitOrder");

        if(result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //??????????????????????????????????????????????????????
            String hosRecordId = jsonObject.getString("hosRecordId");
            //????????????
            Integer number = jsonObject.getInteger("number");;
            //????????????
            String fetchTime = jsonObject.getString("fetchTime");;
            //????????????
            String fetchAddress = jsonObject.getString("fetchAddress");;
            //????????????
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            //??????????????????
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //?????????????????????
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //??????mq?????????????????????????????????

            //????????????
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);

            //????????????
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("SMS_194640721");
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "??????": "??????");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);

            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
            return Long.parseLong(orderInfo.getOutTradeNo());
        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }

    @Override
    public IPage<OrderInfo> getOrdersPage(Page<OrderInfo> page, OrderQueryVo orderQueryVo) {
        String hosName = orderQueryVo.getKeyword();//????????????
        Long patientId = orderQueryVo.getPatientId();//???????????????
        String orderStatus = orderQueryVo.getOrderStatus(); //????????????
        String reserveDate = orderQueryVo.getReserveDate();//????????????
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        QueryWrapper wrapper = new QueryWrapper();
        if(!StringUtils.isEmpty(hosName)) {
            wrapper.like("hosname",hosName);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }

        IPage<OrderInfo> resPage = baseMapper.selectPage(page, wrapper);
        resPage.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });

        return resPage;
    }

    @Override
    public OrderInfo getOrderDetail(Long orderId) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",orderId);
        OrderInfo orderInfo = baseMapper.selectOne(wrapper);
        return orderInfo;
    }

    @Override
    public Map<String, Object> getDetail(Long orderId) {
        HashMap<String, Object> resMap = new HashMap<>();
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",orderId);
        OrderInfo orderInfo = this.packOrderInfo(baseMapper.selectOne(wrapper));
        resMap.put("orderInfo",orderInfo);

        Patient patient = userFeignClient.getPatient(orderInfo.getPatientId());
        resMap.put("patient",patient);
        return resMap;
    }

    @Override
    public Map createNative(Long orderId) {
        try{
            //???redis????????????,???????????????
            Map payMap = (Map)redisTemplate.opsForValue().get(orderId.toString());
            if(null != payMap) return payMap;
            QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("out_trade_no",orderId);
            OrderInfo order = baseMapper.selectOne(wrapper);
            paymentInfoService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());

            //1???????????????
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = order.getReserveDate() + "??????"+ order.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", order.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//?????????????????????,????????????
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");//native????????????????????????
            //2???HTTPClient?????????URL???????????????????????????????????????,??????????????????
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //client????????????
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();//????????????
            //3???????????????????????????,??????map????????????
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4????????????????????????
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));//?????????????????????

            //??????redis???
            if(null != resultMap.get("result_code")) {
                //?????????????????????2????????????????????????2???????????????????????????
                redisTemplate.opsForValue().set(orderId.toString(), map, 120, TimeUnit.MINUTES);
            }
            return map;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OrderInfo getById(Long orderId) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",orderId);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        OrderInfo orderInfo = this.getById(orderId);
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",orderId);
        baseMapper.delete(wrapper);

        SignInfoVo signInfoVo = hospFeignClient.getSignKey(orderInfo.getHoscode());
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        BigInteger bigInteger = new BigInteger(orderInfo.getScheduleId(), 16);
        BigInteger res = new BigInteger("29919475834915000000000000000");
        long id = bigInteger.subtract(res).longValue();
        paramMap.put("scheduleId", id);
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);

        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/updateCancelStatus");

        if (jsonObject.getInteger("code") != 200) {
            throw new YyghException(jsonObject.getString("message"), ResultCodeEnum.FAIL.getCode());
        }else{
            return true;
        }
    }

    @Override
    public Boolean cancelOrderRefund(Long orderId) {
        OrderInfo orderInfo = this.getOrderDetail(orderId);
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if(quitTime.isBeforeNow()){//????????????????????????????????????
            return false;
        }
        SignInfoVo signInfoVo = hospFeignClient.getSignKey(orderInfo.getHoscode());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        BigInteger bigInteger = new BigInteger(orderInfo.getScheduleId(), 16);
        BigInteger res = new BigInteger("29919475834915000000000000000");
        long id = bigInteger.subtract(res).longValue();
        paramMap.put("scheduleId",id);
        paramMap.put("timestamp",HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);

        JSONObject jsonObject = HttpRequestHelper
                .sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/updateCancelStatus");

        if(jsonObject.getInteger("code") != 200){
            throw new YyghException(jsonObject.getString("message"),ResultCodeEnum.FAIL.getCode());
        }else{
            if(OrderStatusEnum.PAID.getStatus() == orderInfo.getOrderStatus()){ //?????????,?????????
                Boolean refund = weiXinService.refund(orderId);//??????
                if(!refund){ //????????????
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }

                //??????????????????
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());//????????????
                this.updateByOrderId(orderInfo);
                //??????mq????????????????????? ???????????????????????????????????????????????????mq???????????????????????????????????????????????????????????????????????????1??????
                OrderMqVo orderMqVo = new OrderMqVo();
                orderMqVo.setScheduleId(orderInfo.getScheduleId());
                //????????????
                MsmVo msmVo = new MsmVo();
                msmVo.setPhone(orderInfo.getPatientPhone());
                msmVo.setTemplateCode("SMS_194640722");
                String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "??????": "??????");
                Map<String,Object> param = new HashMap<String,Object>(){{
                    put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                    put("reserveDate", reserveDate);
                    put("name", orderInfo.getPatientName());
                }};
                msmVo.setParam(param);
                orderMqVo.setMsmVo(msmVo);
                rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
            }
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());//????????????
            this.updateByOrderId(orderInfo);
            return true;
        }
    }

    @Override
    public void patientTip() {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        //????????????????????????????????????????????????????????????
        System.out.println(new DateTime().toString("yyyy-MM-dd"));
        wrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        wrapper.ne("order_status",OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> orderInfoList = baseMapper.selectList(wrapper);

        //????????????????????????
        for(OrderInfo orderInfo : orderInfoList){
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "??????": "??????");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        //????????????????????????
        Map<String, Object> resMap = new HashMap<>();
        //?????????????????????????????????
        List<OrderCountVo> orderCountList = baseMapper.getOrderCount(orderCountQueryVo);

        //??????x?????????
        List<String> dateList = orderCountList.stream()
                .map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //??????y?????????
        List<Integer> countList = orderCountList.stream()
                .map(OrderCountVo::getCount).collect(Collectors.toList());

        //????????????
        resMap.put("dateList",dateList);
        resMap.put("countList",countList);

        return resMap;
    }

    private void updateByOrderId(OrderInfo orderInfo) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",orderInfo.getOutTradeNo());
        baseMapper.update(orderInfo,wrapper);
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString",
                OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

}
