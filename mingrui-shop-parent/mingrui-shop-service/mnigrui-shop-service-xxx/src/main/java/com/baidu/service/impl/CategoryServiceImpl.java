package com.baidu.service.impl;

import com.baidu.mapper.CategoryMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

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

    //查询方法
    @Transactional
    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    //删除放法
    @Transactional
    @Override
    public Result<JsonObject> delcategory(Integer id) {
        //通过id查询当前节点是否为父级节点
        //通过当前id查询当前节点的父节点id
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if (categoryEntity.getId() == null) {
            return this.setResultError("当前节点不存在");
        }

        if (categoryEntity.getIsParent() == 1) {
            return this.setResultError("当前节点是父级节点,不能被删除");
        }

        //查看是否还有节点的父节点是当前节点的父节点id

        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        if (list.size() ==1) {
            CategoryEntity editEntity = new CategoryEntity();

            editEntity.setId(categoryEntity.getParentId());
            editEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(editEntity);
        }
        categoryMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }

    //修改方法
    @Transactional
    @Override
    public Result<JsonObject> editCategory(CategoryEntity entity) {

            categoryMapper.updateByPrimaryKeySelective(entity);

        return this.setResultSuccess();
    }

    //新增方法
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
}