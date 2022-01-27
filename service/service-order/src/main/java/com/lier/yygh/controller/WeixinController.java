package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.enums.PaymentTypeEnum;
import com.lier.yygh.service.OrderInfoService;
import com.lier.yygh.service.PaymentInfoService;
import com.lier.yygh.service.WeiXinService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/22 - 15:20
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("api/order/weixin/")
public class WeixinController {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private WeiXinService WeiXinService;

    @ApiOperation("生成微信支付二维码")
    @GetMapping("createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId){
        Map map = orderInfoService.createNative(orderId);
        return Result.ok(map);
    }

    @ApiOperation("查询支付状态")
    @GetMapping("getPaymentStatus/{orderId}")
    public Result getPaymentStatus(@PathVariable Long orderId){
        Map<String,String> resMap = paymentInfoService
                .getPaymentStatus(orderId, PaymentTypeEnum.WEIXIN.name());

        if(null == resMap){
            return Result.fail().message("支付出错");
        }
        if("SUCCESS".equals(resMap.get("trade_state"))){
            String out_trade_no = resMap.get("out_trade_no");
            paymentInfoService.paySuccess(out_trade_no, resMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }

    @ApiOperation("取消预约--未支付")
    @PutMapping("cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId){
        boolean isSuccess = orderInfoService.cancelOrder(orderId);
        return Result.ok(isSuccess);
    }

    @ApiOperation("微信退款接口")
    @GetMapping("refund/{orderId}")
    public Result refund(@PathVariable Long orderId){
        Boolean isSuccess = WeiXinService.refund(orderId);
        return Result.ok(isSuccess);
    }

}
