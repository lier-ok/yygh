package com.lier.yygh.hospitalmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lier.hospital.mapper.OrderInfoMapper;
import com.lier.hospital.model.OrderInfo;
import com.lier.hospital.util.HttpRequestHelper;
import com.lier.hospital.util.ResultCodeEnum;
import com.lier.hospital.util.YyghException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class HospitalManageApplicationTests {

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Test
    void contextLoads() {
    }


    @org.junit.Test
    public void test1(){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("hoscode","1000_0");
        paramMap.put("scheduleId","618cfe7f5ef1c255240ce401");
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());


        String hoscode = (String)paramMap.get("hoscode");
        String scheduleId = (String)paramMap.get("scheduleId");

        BigInteger bigInteger = new BigInteger(scheduleId,16);
        BigInteger res = new BigInteger("29919475834915000000000000000");
        long id = bigInteger.subtract(res).longValue();

        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("schedule_id",id);
        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);

        if(null == orderInfo) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        //已支付
        orderInfo.setOrderStatus(1);
        orderInfo.setPayTime(new Date());
        orderInfoMapper.updateById(orderInfo);

    }


}
