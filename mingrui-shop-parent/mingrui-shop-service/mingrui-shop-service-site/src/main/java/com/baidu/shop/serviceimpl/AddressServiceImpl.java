package com.baidu.shop.serviceimpl;

import com.baidu.shop.dto.AddressDTO;
import com.baidu.shop.service.AddressService;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.mapper.AddressMapper;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName AddressServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/27
 * @Version V1.0
 **/
@RestController
public class AddressServiceImpl extends BaseApiService implements AddressService {

    @Resource
    private AddressMapper addressMapper;


    @Transactional
    @Override
    public Result<JsonObject> saveOrUpdateAddress(AddressDTO addressDTO) {

        if (ObjectUtil.isNotNull(addressDTO.getId())){
            if(addressDTO.getDefaulta() == true){
                Example example = new Example(AddressDTO.class);
                example.createCriteria().andEqualTo("defaulta",addressDTO.getDefaulta())
                        .andEqualTo("userid",addressDTO.getUserId());
                List<AddressDTO> addressDTOS = addressMapper.selectByExample(example);

                if(addressDTOS.size() > 0)
                    addressMapper.updateDefaulta( addressDTOS.get(0).getId());
            }
            addressMapper.updateByPrimaryKey(addressDTO);
        }else{
            if(addressDTO.getDefaulta() == true){
                Example example = new Example(AddressDTO.class);
                example.createCriteria().andEqualTo("defaulta",addressDTO.getDefaulta())
                        .andEqualTo("userid",addressDTO.getUserId());
                List<AddressDTO> addressDTOS = addressMapper.selectByExample(example);

                if(addressDTOS.size() > 0)
                    addressMapper.updateDefaulta( addressDTOS.get(0).getId());
            }
            addressMapper.insertSelective(addressDTO);
        }

        return this.setResultSuccess();
    }


    @Override
    public Result<List<AddressDTO>> getAddress(Integer userId) {
        Example example = new Example(AddressDTO.class);
        example.createCriteria().andEqualTo("userId",userId);
        List<AddressDTO> addressList = addressMapper.selectByExample(example);
        return this.setResultSuccess(addressList);
    }

    @Override
    public Result<JsonObject> deleteAddress(Integer id) {
        addressMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }



}
