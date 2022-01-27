package com.lier.yygh.util;

import org.springframework.data.mongodb.core.mapping.TextScore;

/**
 * @Author lier
 * @date 2021/11/11 - 17:18
 * @Decription
 * @since jdk1.8
 */
public class Base64Util {

    public static String base64Util(String strOld){
        String[] split = strOld.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < split.length-1; i++) {
            stringBuilder.append(split[i]).append("+");
        }
        stringBuilder.append(split[split.length-1]);
        String strNew = stringBuilder.toString();
        return strNew;
    }

}
