package com.lier.yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lier.yygh.cmn.DictFeignClient;
import com.lier.yygh.enums.DictEnum;
import com.lier.yygh.mapper.PatientMapper;
import com.lier.yygh.model.user.Patient;
import com.lier.yygh.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author lier
 * @date 2021/12/4 - 16:43
 * @Decription
 * @since jdk1.8
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Resource
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> getAllPatients(Long userId) {
        //查询出对应就诊人
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> patients = baseMapper.selectList(wrapper);
        //添加附加信息
        patients.forEach(this::packPatient);
        return patients;
    }

    @Override
    public Patient getPatientById(Long id) {
        Patient patient = baseMapper.selectById(id);
        if(null != patient){
            this.packPatient(patient);
            return patient;
        }else{
            return null;
        }

    }

    private void packPatient(Patient patient) {

        String contactsCertificatesTypeString = null;
        //联系人证件
        String certificatesTypeString =
                    dictFeignClient.getHosType(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        //联系人证件类型
        if(!StringUtils.isEmpty(patient.getContactsCertificatesType())) {
            contactsCertificatesTypeString =
                    dictFeignClient.getHosType(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        }
        //省
        String provinceString = dictFeignClient.getHospitalAddress(patient.getProvinceCode());
        //市
        String cityString = dictFeignClient.getHospitalAddress(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getHospitalAddress(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
    }


}
