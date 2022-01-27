package com.lier.yygh.util;

import com.lier.yygh.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author lier
 * @date 2021/12/4 - 14:50
 * @Decription
 * @since jdk1.8
 */
public class UserInfoHelper {

    //获取用户id
    public static Long getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }
    //获取用户名称
    public static String getUsername(HttpServletRequest request){
        String token = request.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
