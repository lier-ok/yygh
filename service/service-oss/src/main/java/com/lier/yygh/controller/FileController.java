package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.service.FileService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @Author lier
 * @date 2021/12/3 - 18:02
 * @Decription 阿里云oss文件操作接口类
 * @since jdk1.8
 */
@RestController
@RequestMapping("api/oss/file")
public class FileController {

    @Resource
    private FileService fileService;

    @ApiOperation("文件上传")
    @PostMapping("upload")
    public Result fileUpload(MultipartFile file) {

        String url = fileService.upload(file);
        return Result.ok(url);
    }

}
