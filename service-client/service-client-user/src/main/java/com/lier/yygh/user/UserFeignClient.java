package com.lier.yygh.user;

import com.lier.yygh.config.feign.ServiceFeignConfiguration;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.user.Patient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author lier
 * @date 2022/1/18 - 18:49
 * @Decription
 * @since jdk1.8
 */
@FeignClient(value = "service-user",configuration = ServiceFeignConfiguration.class)
@Component
public interface UserFeignClient {

    @ApiOperation("获取就诊人")
    @GetMapping("api/user/patient/getPatient/{patientId}")
    public Patient getPatient(@PathVariable("patientId") Long patientId);
}
