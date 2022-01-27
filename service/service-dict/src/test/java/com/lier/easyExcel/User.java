package com.lier.easyExcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Author lier
 * @date 2021/10/29 - 15:46
 * @Decription
 * @since jdk1.8
 */
@Data
public class User {
    @ExcelProperty(value = "用户Id",index = 0)
    private Integer id;

    @ExcelProperty(value = "用户名",index = 1)
    private String username;
}
