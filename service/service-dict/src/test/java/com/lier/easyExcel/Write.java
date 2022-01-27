package com.lier.easyExcel;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;

/**
 * @Author lier
 * @date 2021/10/29 - 15:42
 * @Decription 写入excel操作
 * @since jdk1.8
 */
public class Write {

    public static void main(String[] args) {
        ArrayList<User> users = new ArrayList<>();
        for(int i = 1; i <= 100;i++){
            User user = new User();
            user.setId(i);
            user.setUsername("lier" + i);
            users.add(user);
        }

        String file = "F:\\easyExcel\\test.xlsx";

        EasyExcel.write(file,User.class).sheet("用户").doWrite(users);
    }
}
