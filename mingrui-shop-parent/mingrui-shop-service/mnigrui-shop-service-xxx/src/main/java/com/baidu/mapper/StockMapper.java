package com.baidu.mapper;

import com.baidu.shop.dto.StockDTO;
import com.baidu.shop.entity.StockEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @ClassName StockMapper
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/8
 * @Version V1.0
 **/
public interface StockMapper extends Mapper<StockEntity>, DeleteByIdListMapper<StockEntity,Long> {

    @Select("update tb_stock t set t.stock = (\n" +
            "\t ( select * from ( select stock from tb_stock where sku_id = #{skuId} ) a ) - #{stock} \n" +
            ") \n" +
            "where t.sku_id =  #{skuId}")
    void updateStockList(Long skuId, Integer stock);
}
