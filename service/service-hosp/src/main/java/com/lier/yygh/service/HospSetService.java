package com.lier.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.model.hosp.HospitalSet;
import com.lier.yygh.vo.order.SignInfoVo;

/**
 * @Author lier
 * @date 2021/10/21 - 22:16
 * @Decription
 * @since jdk1.8
 */
public interface HospSetService extends IService<HospitalSet> {
    String getSignByHoscode(String hoscode);

    SignInfoVo getSignInfo(String hoscode);
}
