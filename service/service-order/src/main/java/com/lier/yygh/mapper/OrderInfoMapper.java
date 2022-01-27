package com.lier.yygh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lier.yygh.model.order.OrderInfo;
import com.lier.yygh.vo.order.OrderCountQueryVo;
import com.lier.yygh.vo.order.OrderCountVo;
import com.lier.yygh.vo.order.OrderQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author lier
 * @date 2022/1/18 - 18:15
 * @Decription
 * @since jdk1.8
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {


    //返回预约统计所需数据
    List<OrderCountVo> getOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}
