package com.lier.yygh.controller.api;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.hosp.Hospital;
import com.lier.yygh.model.hosp.Schedule;
import com.lier.yygh.service.DepartmentService;
import com.lier.yygh.service.HospSetService;
import com.lier.yygh.service.HospitalService;
import com.lier.yygh.service.ScheduleService;
import com.lier.yygh.vo.hosp.DepartmentVo;
import com.lier.yygh.vo.hosp.HospitalQueryVo;
import com.lier.yygh.vo.hosp.ScheduleOrderVo;
import com.lier.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/15 - 20:57
 * @Decription 客户端调用接口
 * @since jdk1.8
 */
@RestController
@RequestMapping("api/hosp/hospital")
public class HospitalApiController {

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private HospSetService hospSetService;

    @ApiOperation("分页查询出所有医院")
    @GetMapping("getHospitalByPage/{page}/{pageSize}")
    public Result getHospitalByPage(@PathVariable("page") Integer page,
                                    @PathVariable("pageSize") Integer pageSize,
                                    HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitalPage = hospitalService.getHospitalPage(page, pageSize, hospitalQueryVo);
        return Result.ok(hospitalPage);
    }

    @ApiOperation("根据医院部分名称模糊查询")
    @GetMapping("getHospitalByName/{hosName}")
    public Result getHospitalByName(@PathVariable String hosName){
        List<Hospital> hospitals = hospitalService.getHospitalByName(hosName);
        return Result.ok(hospitals);
    }

    @ApiOperation("根据医院编号查询医院详情")
    @GetMapping("getDetail/{hoscode}")
    public Result getHospitalDetail(@PathVariable String hoscode){
        Map<String, Object> detail =  hospitalService.getDetailByHoscode(hoscode);
        return Result.ok(detail);
    }

    @ApiOperation("获取科室列表")
    @GetMapping("getDepartment/{hoscode}")
    public Result getDepartment(@PathVariable String hoscode){
        List<DepartmentVo> departments = departmentService.getDepartments(hoscode);
        return Result.ok(departments);
    }

    @ApiOperation("分页查询可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingScheduleRule(@PathVariable Integer page,
                                         @PathVariable Integer limit,
                                         @PathVariable String hoscode,
                                         @PathVariable String depcode){
        Map<String,Object> resultMap = scheduleService.getBookingScheduleRule(page, limit, hoscode,depcode);
        return Result.ok(resultMap);
    }

    @ApiOperation("获取排班数据")
    @GetMapping("/getScheduleData/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleData(@PathVariable String depcode,
                                  @PathVariable String hoscode,
                                  @PathVariable String workDate){
        List<Schedule> scheduleDetail = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return Result.ok(scheduleDetail);
    }

    @ApiOperation("根据id获取排班数据")
    @GetMapping("/getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId){
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return Result.ok(schedule);
    }

    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("/getScheduleOrder/{scheduleId}")
    public ScheduleOrderVo getOrderData(@PathVariable String scheduleId){
        return scheduleService.getOrderVo(scheduleId);
    }

    @ApiOperation("获取医院签名信息")
    @GetMapping("/getSignKey/{hoscode}")
    public SignInfoVo getSignKey(@PathVariable String hoscode){
        return hospSetService.getSignInfo(hoscode);
    }

}
