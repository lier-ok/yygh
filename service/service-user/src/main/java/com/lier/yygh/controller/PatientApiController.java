package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.user.Patient;
import com.lier.yygh.service.PatientService;
import com.lier.yygh.util.UserInfoHelper;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author lier
 * @date 2021/12/4 - 16:41
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("api/user/patient")
public class PatientApiController {

    @Resource
    private PatientService patientService;

    @ApiOperation("查询就诊人列表")
    @GetMapping("findAll")
    public Result getAllPatients(HttpServletRequest request){
        Long userId = UserInfoHelper.getUserId(request);
        userId = 9L;
        List<Patient> patients = patientService.getAllPatients(userId);
        return Result.ok(patients);
    }

    @ApiOperation("添加就诊人")
    @PostMapping("add")
    public Result addPatient(@RequestBody Patient patient,HttpServletRequest request){
        Long userId = UserInfoHelper.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    @ApiOperation("根据指定id获取就诊人详细信息")
    @GetMapping("getById/{id}")
    public Result getPatientById(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    @ApiOperation("修改就诊人信息")
    @PutMapping("update")
    public Result updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }

    @ApiOperation("删除就诊人信息")
    @DeleteMapping("delete/{id}")
    public Result deletePatient(@PathVariable Long id){
        patientService.removeById(id);
        return Result.ok();
    }

    @ApiOperation("获取就诊人")
    @GetMapping("getPatient/{patientId}")
    public Patient getPatient(@PathVariable Long patientId){
        Patient patient= patientService.getPatientById(patientId);
        return patient;
    }
}
