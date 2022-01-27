package com.lier.yygh.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.model.order.OrderInfo;
import com.lier.yygh.vo.order.OrderCountQueryVo;
import com.lier.yygh.vo.order.OrderQueryVo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/18 - 18:18
 * @Decription
 * @since jdk1.8
 */
public interface OrderInfoService extends IService<OrderInfo> {
    @Transactional
    Long saveOrder(String scheduleId, Long patientId);

    IPage<OrderInfo> getOrdersPage(Page<OrderInfo> page, OrderQueryVo orderQueryVo);

    OrderInfo getOrderDetail(Long orderId);

    Map<String, Object> getDetail(Long orderId);


    Map createNative(Long orderId);

    OrderInfo getById(Long orderId);

    @Transactional
    boolean cancelOrder(Long orderId);

    Boolean cancelOrderRefund(Long orderId);

    //就医提醒
    void patientTip();

    //预约统计
    Map<String,Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
