package com.baidu.mapper;

import com.baidu.shop.entity.CategoryEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @ClassName CategoryMapper
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/8/27
 * @Version V1.0
 **/
//可以通过id集合查询
public interface CategoryMapper extends Mapper<CategoryEntity>, SelectByIdListMapper<CategoryEntity,Integer> {
    @Select(value = "select c.id,c.name from tb_category c where c.id in (select cb.category_id from tb_category_brand cb where cb.brand_id=#{brandId})")
    List<CategoryEntity> getByBrandId(Integer brandId);

    @Select(value = "select group_concat(`name` separator \'/\') categoryName from tb_category " +
            "where id in(#{cid1},#{cid2},#{cid3})")
    String getCategoryName(Integer cid1, Integer cid2, Integer cid3);

    /*@Select(value = "select count(1) from tb_category_brand  where category_id = #{id}")
    Integer getByCategoryId(Integer id);*/


}
