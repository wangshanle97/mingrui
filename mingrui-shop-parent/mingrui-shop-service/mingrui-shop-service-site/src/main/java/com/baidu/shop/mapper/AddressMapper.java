package com.baidu.shop.mapper;

import com.baidu.shop.dto.AddressDTO;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

/**
 * @ClassName AddressMapper
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/27
 * @Version V1.0
 **/
public interface AddressMapper extends Mapper<AddressDTO> {

    @Select("update tb_address set defaulta = '0' where id = #{id}")
    void updateDefaulta(Integer id);
}
