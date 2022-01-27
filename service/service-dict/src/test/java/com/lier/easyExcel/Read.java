package com.lier.easyExcel;

import com.alibaba.excel.EasyExcel;

/**
 * @Author lier
 * @date 2021/10/29 - 15:55
 * @Decription
 * @since jdk1.8
 */
public class Read {

    public static void main(String[] args) {
        String file = "F:\\easyExcel\\test.xlsx";

        EasyExcel.read(file,User.class,new EasyExcelListener()).sheet().doRead();
    }
}
