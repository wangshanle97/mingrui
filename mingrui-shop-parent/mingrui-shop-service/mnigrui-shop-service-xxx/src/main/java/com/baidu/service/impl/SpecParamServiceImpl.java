package com.baidu.service.impl;

import com.baidu.mapper.SpecParamMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.service.SpecParamService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SpecParamServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/9
 * @Version V1.0
 **/
@RestController
public class SpecParamServiceImpl extends BaseApiService implements SpecParamService {

    @Resource
    private SpecParamMapper specParamMapper;

    //查询规格组参数
    @Transactional
    @Override
    public Result<List<SpecParamEntity>> getSpecParamInfo(SpecParamDTO specParamDTO) {

        //if(ObjectUtil.isNull(specParamDTO.getGroupId())) return this.setResultError("规格组id不能为空");

        //构建条件查询 通过规格参数id查询
        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();
        //通过规格组id查询
        if(ObjectUtil.isNotNull(specParamDTO.getGroupId())) criteria.andEqualTo("groupId",specParamDTO.getGroupId());

        //通过商品分类ID查询
        if(ObjectUtil.isNotNull(specParamDTO.getCid())) criteria.andEqualTo("cid",specParamDTO.getCid());

        if(ObjectUtil.isNotNull(specParamDTO.getSearching())) criteria.andEqualTo("searching",specParamDTO.getSearching());

        if (ObjectUtil.isNotNull(specParamDTO.getGeneric())) criteria.andEqualTo("generic",specParamDTO.getGeneric());

        List<SpecParamEntity> list = specParamMapper.selectByExample(example);

        return this.setResultSuccess(list);
    }


    //新增规格参数
    @Transactional
    @Override
    public Result<JsonObject> saveSpaecParam(SpecParamDTO specParamDTO) {
        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    //修改规格参数
    @Transactional
    @Override
    public Result<JsonObject> editSpecParam(SpecParamDTO specParamDTO) {
        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    //删除规格参数
    @Transactional
    @Override
    public Result<JsonObject> removeSpecParam(Integer id) {

        specParamMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }
}
