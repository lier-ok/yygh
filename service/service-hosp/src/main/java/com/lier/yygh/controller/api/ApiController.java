package com.lier.yygh.controller.api;

import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.md5.MD5;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.helper.HttpRequestHelper;
import com.lier.yygh.model.hosp.Department;
import com.lier.yygh.model.hosp.Hospital;
import com.lier.yygh.model.hosp.Schedule;
import com.lier.yygh.service.DepartmentService;
import com.lier.yygh.service.HospSetService;
import com.lier.yygh.service.HospitalService;
import com.lier.yygh.service.ScheduleService;
import com.lier.yygh.util.Base64Util;
import com.lier.yygh.vo.hosp.DepartmentQueryVo;
import com.lier.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/9 - 18:05
 * @Decription 模拟医院系统,对接操作
 * @since jdk1.8
 */
@Api("医院")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Resource
    private HospitalService hospitalService;
    @Resource
    private HospSetService hospSetService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private ScheduleService scheduleService;

    @ApiOperation("删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //验证签名
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }
        String hosScheduleId = (String)stringObjectMap.get("hosScheduleId");
        scheduleService.deleteSchedule(hoscode,hosScheduleId);
        return Result.ok();
    }

    @ApiOperation("查询分页排班")
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //验证签名
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }
        int page = stringObjectMap.get("page") == null ? 1 : Integer.parseInt((String)stringObjectMap.get("page"));
        int pageSize = stringObjectMap.get("limit") == null ? 10 : Integer.parseInt((String)stringObjectMap.get("limit"));

        String depcode = (String)stringObjectMap.get("depcode");
        ScheduleQueryVo queryVo = new ScheduleQueryVo();
        queryVo.setHoscode(hoscode);
        queryVo.setDepcode(depcode);
        Page<Schedule> pages = scheduleService.findPage(page,pageSize,queryVo);
        return Result.ok(pages);
    }


    @ApiOperation("添加排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //验证签名
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }

        scheduleService.save(stringObjectMap);
        return Result.ok();
    }

    @ApiOperation("删除指定科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //验证签名
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }

        String depcode = (String)stringObjectMap.get("depcode");
        departmentService.delete(hoscode,depcode);
        return Result.ok();
    }

    @ApiOperation("分页查询科室")
    @PostMapping("department/list")
    public Result findDepartmentByPage(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);

        int page = stringObjectMap.get("page") == null ? 1 : Integer.parseInt((String)stringObjectMap.get("page"));
        int pageSize = stringObjectMap.get("limit") == null ? 10 : Integer.parseInt((String)stringObjectMap.get("limit"));
        String depcode = (String)stringObjectMap.get("depcode");
        //签名验证
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }

        DepartmentQueryVo queryVo = new DepartmentQueryVo();
        queryVo.setHoscode(hoscode);
        queryVo.setDepcode(depcode);
        Page<Department> departments = departmentService.selectPage(page,pageSize,queryVo);
        return Result.ok(departments);
    }

    @ApiOperation("添加科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //验证签名
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }

        departmentService.save(stringObjectMap);
        return Result.ok();
    }


    @ApiOperation("根据hoscode查询医院")
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //签名检验
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }

        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);

        return Result.ok(hospital);
    }

    @ApiOperation("添加医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest httpServletRequest){
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap(); //获取请求过来的数据
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(parameterMap);
        //由于医院logo通过base64编码会自动将"+" 变为 " ",做一下转换
        String logoData = (String) stringObjectMap.get("logoData");
        if(null != logoData){
            String base64 = Base64Util.base64Util(logoData);
            stringObjectMap.put("logoData",base64);
        }
        //获取签名key 该签名已加密
        String signKeyFromRequest = (String)stringObjectMap.get("sign");

        //通过hoscode获取HospSet对应数据库中key
        String hoscode = (String)stringObjectMap.get("hoscode");
        String signKeyByHoscode = hospSetService.getSignByHoscode(hoscode);
        //将查询出的key加密
        String encrypt = MD5.encrypt(signKeyByHoscode.toString());

        //判断签名是否一致
        if(!signKeyFromRequest.equals(encrypt)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);//抛出签名错误异常
        }
        hospitalService.save(stringObjectMap);
        return Result.ok();
    }


}
