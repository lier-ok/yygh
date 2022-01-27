package com.lier.yygh.service.impl;

import com.lier.yygh.service.MsmService;
import com.lier.yygh.util.MsmSendUtil;
import com.lier.yygh.vo.msm.MsmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/20 - 17:06
 * @Decription
 * @since jdk1.8
 */
@Service
public class MsmServiceImpl implements MsmService {

    @Resource
    private MsmSendUtil msmSendUtil;

    @Override
    public Map<String,Object> msmSend(String phone, String validateTime) {
        HashMap<String, Object> result = new HashMap<>();
        if(!StringUtils.isEmpty(phone)){
            String code = msmSendUtil.smsSend(phone, validateTime);
            result.put("code",code);
            result.put("result",true);
            return result;
        }else{
            result.put("code","");
            result.put("result",false);
            return result;
        }
    }

    @Override
    public boolean send(MsmVo msmVo) {
        if(!StringUtils.isEmpty(msmVo.getPhone())){
            this.msmSend(msmVo.getPhone(),"10");
            return true;
        }
        return false;
    }

}
