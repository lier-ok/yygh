package com.lier.yygh;

import com.lier.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/26 - 20:33
 * @Decription
 * @since jdk1.8
 */
@Component
@FeignClient("service-order")
public interface OrderFeignClient {

    @PostMapping("admin/order/orderInfo/getCountMap")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
