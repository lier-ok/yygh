package com.lier.hospital.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lier.hospital.mapper.OrderInfoMapper;
import com.lier.hospital.mapper.ScheduleMapper;
import com.lier.hospital.model.OrderInfo;
import com.lier.hospital.model.Patient;
import com.lier.hospital.model.Schedule;
import com.lier.hospital.reponsitory.ScheduleRepository;
import com.lier.hospital.service.HospitalService;
import com.lier.hospital.util.HttpRequestHelper;
import com.lier.hospital.util.ResultCodeEnum;
import com.lier.hospital.util.YyghException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HospitalServiceImpl implements HospitalService {

	@Autowired
	private ScheduleMapper hospitalMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private ScheduleRepository scheduleRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> submitOrder(Map<String, Object> paramMap) {
        log.info(JSONObject.toJSONString(paramMap));
        String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");
        String hosScheduleId = (String)paramMap.get("hosScheduleId");
        String reserveDate = (String)paramMap.get("reserveDate");
        String reserveTime = (String)paramMap.get("reserveTime");
        String amount = (String)paramMap.get("amount");

        Schedule schedule = this.getSchedule("1");
//        Schedule schedule = this.getTheSchedule(hosScheduleId);
        if(null == schedule) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }

        if(!schedule.getHoscode().equals(hoscode)
                || !schedule.getDepcode().equals(depcode)
                || !schedule.getAmount().toString().equals(amount)) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }

        //???????????????
        Patient patient = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Patient.class);
        log.info(JSONObject.toJSONString(patient));
        //?????????????????????
        Long patientId = this.savePatient(patient);

        Map<String, Object> resultMap = new HashMap<>();
        int availableNumber = schedule.getAvailableNumber().intValue() - 1;
        if(availableNumber > 0) {
            schedule.setAvailableNumber(availableNumber);
            hospitalMapper.updateById(schedule);

            //??????????????????
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setPatientId(patientId);
            BigInteger bigInteger = new BigInteger(hosScheduleId,16);
            BigInteger res = new BigInteger("29919475834915000000000000000");
            orderInfo.setScheduleId(bigInteger.subtract(res).longValue());
            int number = schedule.getReservedNumber().intValue() - schedule.getAvailableNumber().intValue();
            orderInfo.setNumber(number);
            orderInfo.setAmount(new BigDecimal(amount));
            String fetchTime = "0".equals(reserveDate) ? " 09:30???" : " 14:00???";
            orderInfo.setFetchTime(reserveTime + fetchTime);
            orderInfo.setFetchAddress("??????9?????????");
            //?????? ?????????
            orderInfo.setOrderStatus(0);
            orderInfoMapper.insert(orderInfo);

            resultMap.put("resultCode","0000");
            resultMap.put("resultMsg","????????????");
            //??????????????????????????????????????????????????????
            resultMap.put("hosRecordId", orderInfo.getId());
            //????????????
            resultMap.put("number", number);
            //????????????
            resultMap.put("fetchTime", reserveDate + "09:00???");;
            //????????????
            resultMap.put("fetchAddress", "??????114??????");;
            //??????????????????
            resultMap.put("reservedNumber", schedule.getReservedNumber());
            //?????????????????????
            resultMap.put("availableNumber", schedule.getAvailableNumber());
        } else {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        return resultMap;
    }
    private Schedule getTheSchedule(String scheduleId){
        return scheduleRepository.findById(scheduleId).get();
    }


    @Override
    public void updatePayStatus(Map<String, Object> paramMap) {
        String hoscode = (String)paramMap.get("hoscode");
        String scheduleId = (String)paramMap.get("scheduleId");
        OrderInfo orderInfo = this.selectByHosRecordId(Long.parseLong(scheduleId));
        if(null == orderInfo) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        //?????????
        orderInfo.setOrderStatus(1);
        orderInfo.setPayTime(new Date());
        orderInfoMapper.updateById(orderInfo);
    }

    private OrderInfo selectByHosRecordId(Long id) {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("schedule_id",id);
        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        return orderInfo;
    }



    @Override
    public void updateCancelStatus(Map<String, Object> paramMap) {
        String hoscode = (String)paramMap.get("hoscode");
        String scheduleId = (String)paramMap.get("scheduleId");

        OrderInfo orderInfo = this.selectByScheduleId(scheduleId);
        if(null == orderInfo) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        //?????????
        orderInfo.setOrderStatus(-1);
        orderInfo.setQuitTime(new Date());
        orderInfoMapper.updateById(orderInfo);
    }

    private OrderInfo selectByScheduleId(String scheduleId){
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("schedule_id",scheduleId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        return orderInfo;
    }

    private Schedule getSchedule(String frontSchId) {
        return hospitalMapper.selectById(frontSchId);
    }

    /**
     * ???????????????????????????
     * @param patient
     */
    private Long savePatient(Patient patient) {
        // ????????????
        return 1L;
    }


}
