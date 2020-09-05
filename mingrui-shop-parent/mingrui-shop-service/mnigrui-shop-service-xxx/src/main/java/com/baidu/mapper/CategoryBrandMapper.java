package com.baidu.mapper;

import com.baidu.shop.entity.CategoryBrandEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @ClassName CategoryBrandMapper
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/1
 * @Version V1.0
 **/
public interface CategoryBrandMapper extends Mapper<CategoryBrandEntity>, InsertListMapper<CategoryBrandEntity> {



}
