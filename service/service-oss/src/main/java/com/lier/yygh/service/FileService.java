package com.lier.yygh.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author lier
 * @date 2021/12/3 - 18:04
 * @Decription
 * @since jdk1.8
 */
public interface FileService {
    String upload(MultipartFile file);
}
