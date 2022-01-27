package com.lier.yygh.controller;

import com.lier.yygh.OrderFeignClient;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author lier
 * @date 2022/1/26 - 20:43
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public Result getCountMap(OrderCountQueryVo orderCountQueryVo) {
        return Result.ok(orderFeignClient.getCountMap(orderCountQueryVo));
    }

}
