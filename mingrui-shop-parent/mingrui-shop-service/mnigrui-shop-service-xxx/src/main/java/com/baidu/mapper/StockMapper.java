package com.baidu.mapper;

import com.baidu.shop.entity.StockEntity;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ClassName StockMapper
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/8
 * @Version V1.0
 **/
public interface StockMapper extends Mapper<StockEntity>, DeleteByIdListMapper<StockEntity,Long> {

}
