package com.lier.yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.mapper.HospSetMapper;
import com.lier.yygh.model.hosp.HospitalSet;
import com.lier.yygh.service.HospSetService;
import com.lier.yygh.vo.order.SignInfoVo;
import org.springframework.stereotype.Service;

/**
 * @Author lier
 * @date 2021/10/21 - 22:16
 * @Decription
 * @since jdk1.8
 */
@Service
public class HospSetServiceImpl extends ServiceImpl<HospSetMapper, HospitalSet> implements HospSetService {


    @Override
    public String getSignByHoscode(String hoscode) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }

    @Override
    public SignInfoVo getSignInfo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(hospitalSet == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }
}
