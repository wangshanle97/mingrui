package com.baidu.service.impl;

import com.baidu.mapper.SpecGroupMapper;
import com.baidu.mapper.SpecParamMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.exceotion.BaiduException;
import com.baidu.shop.service.SpecGroupService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SpecGroupServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecGroupServiceImpl extends BaseApiService implements SpecGroupService {

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private SpecParamMapper specParamMapper;

    @Override
    public Result<List<SpecGroupEntity>> query(SpecGroupDTO specGroupDTO) {

        Example example = new Example(SpecGroupEntity.class);
        if (ObjectUtil.isNotNull(specGroupDTO.getCid())) example.createCriteria().andEqualTo("cid",specGroupDTO.getCid());

        List<SpecGroupEntity> specGroupEntities = specGroupMapper.selectByExample(example);
        return this.setResultSuccess(specGroupEntities);
    }

    @Override
    public Result<JsonObject> save(SpecGroupDTO specGroupDTO) {

        specGroupMapper.insertSelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> edit(SpecGroupDTO specGroupDTO) {
        specGroupMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specGroupDTO,SpecGroupEntity.class));

        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> remove(Integer id) {
        SpecParamEntity specParamEntity = new SpecParamEntity();
        specParamEntity.setGroupId(id);
        Example example1 = new Example(SpecParamEntity.class);

        example1.createCriteria().andEqualTo("groupId",specParamEntity.getGroupId());
        List<SpecParamEntity> list = specParamMapper.selectByExample(example1);
        if(list.size() >= 1) {
            return this.setResultError("当前规格不能被删除,里面有内容");
        }else{
            specGroupMapper.deleteByPrimaryKey(id);
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<List<SpecParamEntity>> getSpecParamInfo(SpecParamDTO specParamDTO) {

        //if(ObjectUtil.isNull(specParamDTO.getGroupId())) return this.setResultError("规格组id不能为空");

        if(ObjectUtil.isNull(specParamDTO.getGroupId())) throw new BaiduException("规格组id不能为空");

        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId",specParamDTO.getGroupId());

        List<SpecParamEntity> list = specParamMapper.selectByExample(example);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<JsonObject> saveSpaecParam(SpecParamDTO specParamDTO) {
        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> editSpecParam(SpecParamDTO specParamDTO) {
        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> removeSpecParam(Integer id) {

        specParamMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }
}
