package com.lier.yygh.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.service.FileService;
import com.lier.yygh.util.ConstantOssProperties;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * @Author lier
 * @date 2021/12/3 - 18:05
 * @Decription
 * @since jdk1.8
 */
@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile file) {
        //获取配置文件中的值
        String endpoint = ConstantOssProperties.ENDPOINT;
        String accessKeyId = ConstantOssProperties.ACCESS_KEY_ID;
        String secret = ConstantOssProperties.SECRET;
        String bucket = ConstantOssProperties.BUCKET;

        try{
            //创建ossClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, secret);
            //获取文件输入流,文件名
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            //组装文件名使其唯一,避免在oss存储器中被覆盖,
            String uuid = UUID.randomUUID().toString()
                    .replaceAll("-","");//替换uuid中的-
            fileName = uuid + fileName;
            //为文件添加当前时间作为目录,进行日期的分组
            String time = new DateTime().toString("yyyy/MM/dd");
            fileName = time + "/" + fileName;
            //ossClient实例调用方法上传
            ossClient.putObject(bucket, fileName, inputStream);
            ossClient.shutdown();
            //返回图片url地址
            String url = "https://" + bucket + "." + endpoint + "/" + fileName;
            return url;
        }catch(Exception e){
            e.printStackTrace();
            throw new YyghException(ResultCodeEnum.URL_ISNULL);
        }
    }
}
