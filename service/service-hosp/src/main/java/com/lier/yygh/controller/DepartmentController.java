package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.service.DepartmentService;
import com.lier.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author lier
 * @date 2021/11/14 - 16:17
 * @Decription 医院部门操作
 * @since jdk1.8
 */
@RestController
@RequestMapping("/admin/hosp/department")
//@CrossOrigin
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @ApiOperation("获取科室信息")
    @GetMapping("getDeptByHoscode/{hoscode}")
    public Result getDeptByHoscode(@PathVariable String hoscode){
        List<DepartmentVo> result = departmentService.getDepartments(hoscode);
        return Result.ok(result);
    }
}
