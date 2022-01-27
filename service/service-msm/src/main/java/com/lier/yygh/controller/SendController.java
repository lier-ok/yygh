package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.service.MsmService;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author lier
 * @date 2021/11/20 - 17:25
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("/api/msm")
public class SendController {

    @Resource
    private MsmService msmService;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @ApiOperation("发送短信验证码")
    @GetMapping("send/{phone}")
    public Result send(@PathVariable String phone) {
        //从redis中获取验证码,看是否存在
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)){ //存在
            return Result.ok();
        }
        //发送验证码,验证码验证时间提示为一分钟
        Map<String, Object> result = msmService.msmSend(phone, "1");
        code = (String) result.get("code");
        boolean res = (boolean) result.get("result");
        if(res){
            //存入redis中并设置过期时间一分钟
            redisTemplate.opsForValue().set(phone,code,1, TimeUnit.MINUTES);
            return Result.ok();
        }else{
            return Result.fail().message("短信验证码发送失败");
        }
    }
}
