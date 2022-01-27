package com.lier.yygh.cmn;

import com.lier.yygh.config.feign.ServiceFeignConfiguration;
import com.lier.yygh.config.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author lier
 * @date 2021/11/11 - 21:34
 * @Decription
 * @since jdk1.8
 */
@FeignClient(value = "service-dict",configuration = ServiceFeignConfiguration.class)
@Component
public interface DictFeignClient {

    @ApiOperation("查询医院等级")
    @GetMapping("/admin/cmn/dict/getHosType/{dictCode}/{value}")
    public String getHosType(@PathVariable(value = "dictCode") String dictCode,
                             @PathVariable(value = "value") String value);

    @ApiOperation("查询医院地址")
    @GetMapping("/admin/cmn/dict/getHospAddress/{value}")
    public String getHospitalAddress( @PathVariable(value = "value") String value);
}
