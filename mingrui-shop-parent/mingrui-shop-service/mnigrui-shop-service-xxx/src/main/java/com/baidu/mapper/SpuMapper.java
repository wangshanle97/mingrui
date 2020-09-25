package com.baidu.mapper;

import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

public interface SpuMapper extends Mapper<SpuEntity> {
    /*@Select(value = "SELECT\n" +
            " s.*,\n" +
            " brand_name,\n" +
            " category_name \n" +
            "FROM\n" +
            " tb_spu s,\n" +
            " (\n" +
            " SELECT\n" +
            "  s.id spuid,\n" +
            "  b.`name` brand_name,\n" +
            "  concat_ws(\n" +
            "   '/',\n" +
            "   MAX( CASE s.cid1 WHEN y.id THEN y.NAME ELSE 0 END ),\n" +
            "   MAX( CASE s.cid2 WHEN y.id THEN y.NAME ELSE 0 END ),\n" +
            "   MAX( CASE s.cid3 WHEN y.id THEN y.NAME ELSE 0 END ) \n" +
            "  ) category_name \n" +
            " FROM\n" +
            "  tb_category y,\n" +
            "  tb_spu s,\n" +
            "  tb_brand b \n" +
            " WHERE\n" +
            "  b.id = s.brand_id \n" +
            " GROUP BY\n" +
            "  b.`name`,\n" +
            "  s.id \n" +
            " ) a \n" +
            "WHERE\n" +
            " s.id = spuid")*/
    //List<SpuDTO> getSpu(Example example);

    List<SpuDTO> getSpuOrGorupList(SpuDTO spuDTO);
}
