package com.lier.yygh.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lier.yygh.config.md5.MD5;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.hosp.HospitalSet;
import com.lier.yygh.service.HospSetService;
import com.lier.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * @Author lier
 * @date 2021/10/21 - 22:34
 * @Decription 医院设置操作
 * @since jdk1.8
 */
@Api("医院设置信息")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin //解决跨域
public class HospSetController {

    @Resource
    private HospSetService hospSetService;


    //查询hospital_set所有记录
    @ApiOperation("查询hospital_set所有记录")
    @GetMapping("findAll")
    public Result fiadAllHospitalSet(){
        List<HospitalSet> list = hospSetService.list();
        //测试全局异常处理
//        try {
//
//            int age = 10/0;
//        }catch (Exception e){
//            throw new YyghException("失败",201);
//        }
        Result<List<HospitalSet>> result = Result.ok(list);
        return result;
    }

    //删除hospital_set指定id记录
    @ApiOperation("删除hospital_set指定id记录")
    @DeleteMapping("{id}")
    public Result deleteById(@PathVariable(value = "id") Long id){
        boolean isSuccess = hospSetService.removeById(id);
        if(isSuccess){
            return Result.ok();
        }
        return Result.fail();
    }

    //带条件的分页查询
    @ApiOperation("带条件的分页查询")
    @PostMapping("findPage/{current}/{limit}") //current:当前页,limit:每页显示条数
    public Result findPageHospSet(@PathVariable("current") Long current,
                                  @PathVariable("limit") Long limit,
                                  @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){ //HospitalSetQueryVo 封装查询条件的实体类
        Page<HospitalSet> page = new Page<HospitalSet>(current, limit);

        String hoscode = hospitalSetQueryVo.getHoscode();//医院编号
        String hosname = hospitalSetQueryVo.getHosname();//医院名称

        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();

        if(!StringUtils.isEmpty(hoscode)){
            wrapper.eq("hoscode",hoscode);
        }
        if(!StringUtils.isEmpty(hosname)){
            wrapper.like("hosname",hosname);
        }

        Page<HospitalSet> hospitalSetPage = hospSetService.page(page, wrapper);

        Result<Page<HospitalSet>> pageResult = Result.ok(hospitalSetPage);

        return pageResult;

    }

    //添加医院设置
    @ApiOperation("添加医院")
    @PostMapping("saveHospSet")
    public Result saveHospSet(@RequestBody HospitalSet hospitalSet){
        hospitalSet.setStatus(1); //状态为1: 可以使用, 0 :不能使用
        Random random = new Random();//生成随机数
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" +
                random.nextInt(1000)));//MD5加密
        boolean save = hospSetService.save(hospitalSet);
        if(save){
            return Result.ok();
        }
        return Result.fail();
    }

    //根据id查询
    @ApiOperation("根据id查询")
    @GetMapping("getHospSetById/{id}")
    public Result getHospSetById(@PathVariable Long id){
        HospitalSet hospitalSet = hospSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    //修改医院
    @ApiOperation("修改医院")
    @PostMapping("update")
    public Result update(@RequestBody HospitalSet hospitalSet){
        boolean b = hospSetService.updateById(hospitalSet);
        if(b){
            return Result.ok();
        }
        return Result.fail();
    }
    //批量删除医院设置
    @ApiOperation("批量删除医院设置")
    @DeleteMapping("deleteBatch")
    public Result deleteBatch(@RequestBody List<Long> idList){
        boolean b = hospSetService.removeByIds(idList);
        if(b){
            return Result.ok();
        }
        return Result.fail();
    }

    //医院的锁定与解锁
    @ApiOperation("医院的锁定与解锁")
    @PutMapping("lockOrUnlock/{id}/{status}")
    public Result HospSetLockOrUnlock(@PathVariable("id") Long id,
                                      @PathVariable("status") Integer status){
        HospitalSet hospitalSet = hospSetService.getById(id);
        hospitalSet.setStatus(status);
        hospSetService.updateById(hospitalSet);
        return Result.ok();
    }

    //发送签名Key
    @ApiOperation("发送签名Key")
    @PutMapping("sendKey/{id}")
    public Result sendKey(@PathVariable Long id){
        HospitalSet hospitalSet = hospSetService.getById(id);
        String hosname = hospitalSet.getHosname();
        String hoscode = hospitalSet.getHoscode();
        String signKey = hospitalSet.getSignKey();
        //发送短信
        return Result.ok();
    }
}
