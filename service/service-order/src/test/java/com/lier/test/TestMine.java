package com.lier.test;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.helper.HttpRequestHelper;
import com.lier.yygh.model.order.OrderInfo;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/23 - 14:45
 * @Decription
 * @since jdk1.8
 */
@SpringBootTest
public class TestMine {


    @Test
    public void test(){
        String str = "618cfe7f5ef1c255240ce400";

        BigInteger bigInteger = new BigInteger(str,16);
        BigInteger res = new BigInteger("29919475834915000000000000000");
        long id = bigInteger.subtract(res).longValue();
        System.out.println(id);
    }


}
