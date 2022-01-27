package com.lier.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lier.yygh.cmn.DictFeignClient;
import com.lier.yygh.enums.DictEnum;
import com.lier.yygh.model.hosp.Hospital;
import com.lier.yygh.repository.HospitalMongoRepository;
import com.lier.yygh.service.HospitalService;
import com.lier.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author lier
 * @date 2021/11/9 - 18:04
 * @Decription
 * @since jdk1.8
 */
@Service
public class HospitalServiceImpl implements HospitalService {

    @Resource
    private HospitalMongoRepository hospitalRepository;

    @Resource
    private DictFeignClient dictFeignClient;


    @Override
    public void save(Map<String, Object> stringObjectMap) {
        //先将map集合转换为对象实例
        String userString = JSONObject.toJSONString(stringObjectMap);
        Hospital hospital = JSONObject.parseObject(userString, Hospital.class);

        //根据hoscode查询数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);

        //判断是否存在在mongodb数据库中
        if(hospitalExist == null){ //不存在 添加
            hospital.setStatus(0); //0 :未上线(默认) 1:已上线
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else{// 存在 修改
            hospital.setStatus(hospital.getStatus());
            hospital.setCreateTime(hospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospitalByHoscode;
    }

    @Override
    public Page<Hospital> getHospitalPage(Integer page, Integer pageSize, HospitalQueryVo queryVo) {
        PageRequest pageCondition = PageRequest.of(page - 1, pageSize);

        ExampleMatcher macher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(queryVo,hospital);
        Example<Hospital> example = Example.of(hospital, macher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageCondition);
        pages.getContent().stream().forEach(this::setHospParam);

        return pages;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    @Override
    public Map<String, Object> getDetailById(String id) {
        Hospital hospital = hospitalRepository.findById(id).get();
        this.setHospParam(hospital);

        HashMap<String, Object> map = new HashMap<>();
        map.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        map.put("hospital",hospital);
        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital != null){
            return hospital.getHosname();
        }
        return null;
    }

    @Override
    public List<Hospital> getHospitalByName(String hosName) {
        List<Hospital> result = hospitalRepository.getHospitalByHosnameLike(hosName);
        return result;
    }

    @Override
    public Map<String, Object> getDetailByHoscode(String hoscode) {
        Map<String, Object> result = new HashMap<>();
        Hospital hospital = this.getHospitalByHoscode(hoscode);
        this.setHospParam(hospital);

        result.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        result.put("hospital",hospital);
        return result;
    }

    private Hospital getByHoscode(String hoscode) {
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospitalByHoscode == null){
            return null;
        }
        return hospitalByHoscode;
    }


    private void setHospParam(Hospital hospItem) { //对医院的param添加参数
        String hosType = dictFeignClient.getHosType(DictEnum.HOSTYPE.getDictCode(), hospItem.getHostype());
        String province = dictFeignClient.getHospitalAddress(hospItem.getProvinceCode());
        String city = dictFeignClient.getHospitalAddress(hospItem.getCityCode());
        String district = dictFeignClient.getHospitalAddress(hospItem.getDistrictCode());

        hospItem.getParam().put("hosType",hosType);
        hospItem.getParam().put("address",province + city + district);
    }
}
