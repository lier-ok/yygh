package com.lier.yygh.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.enums.OrderStatusEnum;
import com.lier.yygh.model.order.OrderInfo;
import com.lier.yygh.service.OrderInfoService;
import com.lier.yygh.util.UserInfoHelper;
import com.lier.yygh.vo.order.OrderCountQueryVo;
import com.lier.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/22 - 13:09
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("admin/order/orderInfo")
public class OrderController {

    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("订单列表")
    @GetMapping("/{pageNo}/{pageSize}")
    public Result getOrderList(@PathVariable Long pageNo, @PathVariable Long pageSize,
                               OrderQueryVo orderQueryVo, HttpServletRequest request){
        Page<OrderInfo> page = new Page(pageNo, pageSize);
        IPage<OrderInfo> pageParam = orderInfoService.getOrdersPage(page,orderQueryVo);
        return Result.ok(pageParam);
    }

    @ApiOperation("订单所有状态")
    @GetMapping("getAllStatus")
    public Result getStatus(){
        return Result.ok(OrderStatusEnum.getStatusList());
    }


    @ApiOperation("订单详情")
    @GetMapping("orderDetail/{orderId}")
    public Result getOrderDetail(@PathVariable Long orderId){
        Map<String,Object> resMap = orderInfoService.getDetail(orderId);
        return Result.ok(resMap);
    }

    @ApiOperation("取消订单--已支付")
    @GetMapping("cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId){
        Boolean isSuccess = orderInfoService.cancelOrderRefund(orderId);
        return Result.ok(isSuccess);
    }

    @ApiOperation("返回预约统计数据")
    @PostMapping("getCountMap")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return orderInfoService.getCountMap(orderCountQueryVo);
    }
}
