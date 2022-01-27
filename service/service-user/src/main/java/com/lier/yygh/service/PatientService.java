package com.lier.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.model.user.Patient;
import com.lier.yygh.model.user.UserInfo;

import java.util.List;

/**
 * @Author lier
 * @date 2021/12/4 - 16:43
 * @Decription
 * @since jdk1.8
 */
public interface PatientService extends IService<Patient> {
    List<Patient> getAllPatients(Long userId);

    Patient getPatientById(Long id);
}
