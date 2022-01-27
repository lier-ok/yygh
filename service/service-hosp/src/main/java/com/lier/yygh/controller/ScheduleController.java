package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.hosp.Schedule;
import com.lier.yygh.service.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/14 - 20:51
 * @Decription 医院排班操作
 * @since jdk1.8
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @ApiOperation("查询排班")
    @GetMapping("getSchedule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleByPage(@PathVariable("page") Integer page,
                                    @PathVariable("limit") Integer limit,
                                    @PathVariable("hoscode") String hoscode,
                                    @PathVariable("depcode") String depcode){

        Map<String,Object> map = scheduleService.findSchedule(page,limit,hoscode,depcode);
        return Result.ok(map);
    }

    @ApiOperation("查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable("hoscode") String hoscode,
                                    @PathVariable("depcode") String depcode,
                                    @PathVariable("workDate") String workDate){
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode,depcode,workDate);
        return Result.ok(list);
    }
}
