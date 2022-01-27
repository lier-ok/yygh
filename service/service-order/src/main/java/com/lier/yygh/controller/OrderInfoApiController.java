package com.lier.yygh.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.enums.OrderStatusEnum;
import com.lier.yygh.model.order.OrderInfo;
import com.lier.yygh.service.OrderInfoService;
import com.lier.yygh.util.UserInfoHelper;
import com.lier.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import sun.security.krb5.internal.AuthContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author lier
 * @date 2022/1/18 - 18:24
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoApiController {

    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("创建订单")
    @PutMapping("auth/saveOrder/{scheduleId}/{patientId}")
    public Result createOrder(@PathVariable String scheduleId, @PathVariable Long patientId){
        Long aLong = orderInfoService.saveOrder(scheduleId, patientId);
        return Result.ok(aLong);
    }

    @ApiOperation("订单列表")
    @GetMapping("auth/OrderList/{pageNo}/{pageSize}")
    public Result getOrderList(@PathVariable Long pageNo, @PathVariable Long pageSize,
                                OrderQueryVo orderQueryVo, HttpServletRequest request){
        Long userId = UserInfoHelper.getUserId(request);
        orderQueryVo.setUserId(userId);
        Page<OrderInfo> page = new Page(pageNo, pageSize);
        IPage<OrderInfo> pageParam = orderInfoService.getOrdersPage(page,orderQueryVo);
        return Result.ok(pageParam);
    }

    @ApiOperation("订单所有状态")
    @GetMapping("auth/getAllStatus")
    public Result getStatus(){
        return Result.ok(OrderStatusEnum.getStatusList());
    }


    @ApiOperation("订单详情")
    @GetMapping("auth/orderDetail/{orderId}")
    public Result getOrderDetail(@PathVariable Long orderId){
        OrderInfo orderInfo = orderInfoService.getOrderDetail(orderId);
        return Result.ok(orderInfo);
    }
}
