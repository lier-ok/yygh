package com.lier.yygh;

import com.lier.yygh.config.feign.ServiceFeignConfiguration;
import com.lier.yygh.vo.hosp.ScheduleOrderVo;
import com.lier.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author lier
 * @date 2022/1/18 - 20:09
 * @Decription
 * @since jdk1.8
 */
@Component
@FeignClient(value = "service-hosp",configuration = ServiceFeignConfiguration.class)
public interface HospFeignClient {

    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("api/hosp/hospital/getScheduleOrder/{scheduleId}")
    public ScheduleOrderVo getOrderData(@PathVariable("scheduleId") String scheduleId);

    @ApiOperation("获取医院签名信息")
    @GetMapping("api/hosp/hospital/getSignKey/{hoscode}")
    public SignInfoVo getSignKey(@PathVariable("hoscode") String hoscode);
}
