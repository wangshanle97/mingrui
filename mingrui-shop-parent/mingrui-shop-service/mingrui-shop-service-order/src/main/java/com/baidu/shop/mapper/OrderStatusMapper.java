package com.baidu.shop.mapper;

import com.baidu.shop.entity.OrderStatusEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface OrderStatusMapper extends Mapper<OrderStatusEntity> {
//    @Select(value = "select order_id,status,create_time from tb_order_status where order_id =#{orderId} and `status` = #{status} ")
//    OrderStatusEntity getStatu(String orderId,Integer status);
}
