package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import com.lier.yygh.service.DictService;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lier
 * @date 2021/10/21 - 22:34
 * @Decription 数据字典
 * @since jdk1.8
 */
@Api(value = "数据字典")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin //解决跨域
public class DictController {

    @Resource
    private DictService dictService;

    //根据id查询其子节点下数据
    @ApiOperation("根据id查询其子节点下数据")
    @GetMapping("findChild/{id}")
    public Result findChildById(@PathVariable("id") Long id){
        List<Dict> dicts = dictService.selectListById(id);
        return Result.ok(dicts);
    }

    //导出数据字典功能
    @ApiOperation("导出数据字典功能")
    @GetMapping("export")
    public void exportDict(HttpServletResponse response){
        dictService.exportDict(response);
    }

    //导入数据功能
    @ApiOperation("导入数据功能")
    @PostMapping("import")
    public Result importDict(MultipartFile file){
        dictService.importFile(file);
        return Result.ok();
    }

    @ApiOperation("查询医院等级")
    @GetMapping("getHosType/{dictCode}/{value}")
    public String getHosType(@PathVariable String dictCode,
                             @PathVariable String value){
        String hosType = dictService.getMsgForHospital(dictCode,value);
        return hosType;
    }

    @ApiOperation("查询医院地址")
    @GetMapping("getHospAddress/{value}")
    public String getHospitalAddress(@PathVariable String value){
        String address = dictService.getMsgForHospital("",value);
        return address;
    }

    @ApiOperation("根据dictcode查询其子节点")
    @GetMapping("getChildByHosType/{dictCode}")
    public Result getChildByHosType(@PathVariable String dictCode){
        List<Dict> dicts = dictService.getHospChildByHosType(dictCode);
        return Result.ok(dicts);
    }


}
