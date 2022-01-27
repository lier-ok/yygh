package com.lier.yygh.service;

import com.lier.yygh.vo.msm.MsmVo;

import java.util.Map;

/**
 * 短信服务接口
 * Date 2018/11/10
 * @Auther 阳彦刚
 */
public interface MsmService {
    Map<String,Object> msmSend(String phone, String validateTime);

    boolean send(MsmVo msmVo);
}
