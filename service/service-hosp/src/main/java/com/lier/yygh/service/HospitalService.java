package com.lier.yygh.service;

import com.lier.yygh.model.hosp.Hospital;
import com.lier.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/9 - 18:03
 * @Decription
 * @since jdk1.8
 */
public interface HospitalService {
    void save(Map<String, Object> stringObjectMap);

    Hospital getHospitalByHoscode(String hoscode);

    Page<Hospital> getHospitalPage(Integer page, Integer pageSize, HospitalQueryVo queryVo);

    void updateStatus(String id, Integer status);

    Map<String, Object> getDetailById(String id);

    String getHospName(String hoscode);

    List<Hospital> getHospitalByName(String hosName);

    Map<String, Object> getDetailByHoscode(String hoscode);
}
