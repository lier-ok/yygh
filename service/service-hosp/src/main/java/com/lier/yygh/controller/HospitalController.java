package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.hosp.Hospital;
import com.lier.yygh.service.HospitalService;
import com.lier.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/11 - 20:38
 * @Decription 医院操作
 * @since jdk1.8
 */
@Api("医院信息")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {

    @Resource
    private HospitalService hospitalService;

    @ApiOperation("医院信息查询--分页")
    @GetMapping("getHospitalPage/{page}/{pageSize}")
    public Result getHospital(@PathVariable(value = "page") Integer page,
                              @PathVariable(value = "pageSize") Integer pageSize,
                              HospitalQueryVo queryVo){

        Page<Hospital> pages = hospitalService.getHospitalPage(page,pageSize,queryVo);
        return Result.ok(pages);
    }

    @ApiOperation("医院上下线功能")
    @PostMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable String id,
                               @PathVariable Integer status){
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }

    @ApiOperation("查看医院详情")
    @GetMapping("getDetail/{id}")
    public Result getHospitalDetail(@PathVariable String id){
        Map<String,Object> map = hospitalService.getDetailById(id);
        return Result.ok(map);
    }

}
