package com.baidu.service.impl;

import com.baidu.mapper.CategoryBrandMapper;
import com.baidu.mapper.CategoryMapper;
import com.baidu.mapper.SpecGroupMapper;
import com.baidu.mapper.SpecificationsMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.service.CategoryService;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpecificationsMapper specificationsMapper;

    @Resource
    private SpecGroupMapper specGroupMapper;

    //查询方法
    @Transactional
    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        //查询品牌
        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    //删除放法
    @Transactional
    @Override
    public Result<JsonObject> delcategory(Integer id) {
        //通过id查询当前节点是否为父级节点
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if (categoryEntity.getId() == null) return this.setResultError("当前节点不存在");

        //查看当前节点是否是父级节点
        if (categoryEntity.getIsParent() == 1) return this.setResultError("当前节点 ---是父级节点,不能被删除");

        //查询是否被品牌绑定
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> list1 = categoryBrandMapper.selectByExample(example1);

        if (list1.size() >= 1)  return this.setResultError("当前分类被其他品牌绑定,不能被删除!");

        //查询是否被规格组绑定
        Example example2 = new Example(SpecGroupEntity.class);
        example2.createCriteria().andEqualTo("cid",id);
        List<SpecGroupEntity> specGroupEntities = specGroupMapper.selectByExample(example2);

        if (specGroupEntities.size() >= 1) return this.setResultError("被规格组绑定无法删除");

        //查询当前节点下是否有子级节点
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        if (list.size() ==1) {
            CategoryEntity editEntity = new CategoryEntity();

            editEntity.setId(categoryEntity.getParentId());
            editEntity.setIsParent(0);
            //将当前节点强行修改为子级节点
            categoryMapper.updateByPrimaryKeySelective(editEntity);
        }
        categoryMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }



    //修改分类方法
    @Transactional
    @Override
    public Result<JsonObject> editCategory(CategoryEntity entity) {

        categoryMapper.updateByPrimaryKeySelective(entity);

        return this.setResultSuccess();
    }

    //新增分类方法
    @Transactional
    @Override
    public Result<JsonObject> addCategory(CategoryEntity entity) {

        CategoryEntity entity1 = new CategoryEntity();
        entity1.setId(entity.getParentId());
        entity1.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(entity1);

        categoryMapper.insertSelective(entity);

        return this.setResultSuccess();
    }

    //通过商品查询分类
    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {
        List<CategoryEntity> list = categoryMapper.getByBrandId(brandId);
        return setResultSuccess(list);
    }

    @Override
    public Result<List<CategoryEntity>> getCategoryByIdList(String ids) {
        List<Integer> cidArr = Arrays.asList(ids.split(",")).stream().map(idStr -> Integer.parseInt(idStr)).collect(Collectors.toList());
        List<CategoryEntity> list = categoryMapper.selectByIdList(cidArr);
        return this.setResultSuccess(list);
    }
}
