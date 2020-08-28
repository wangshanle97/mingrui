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
import org.springframework.web.bind.annotation.RestController;

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

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<JsonObject> delcategory(Integer id) {
        //通过id查询当前节点是否为父级节点
        //通过当前id查询当前节点的父节点id
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if(ObjectUtil.inNull(categoryEntity)){
            return this.setResultError(HTTPStatus.OPERATION_ERROR,"当前id不存在");
        }
        if(categoryEntity.getIsParent() ==1 ){
            return this.setResultError(HTTPStatus.OPERATION_ERROR,"当前节点为父节点");
        }
        //查看是否还有节点的父节点是当前节点的父节点id
        Result<List<CategoryEntity>> result = this.getCategoryByPid(categoryEntity.getParentId());

        if(result.getCode() == HTTPStatus.OK){
            List<CategoryEntity> data = result.getData();

            if(data.size() == 1){//如果没有的话删除成功后需要将父节点的isParent修改为0
                CategoryEntity editEntity = new CategoryEntity();

                editEntity.setId(categoryEntity.getParentId());
                editEntity.setParentId(0);

                categoryMapper.updateByPrimaryKeySelective(editEntity);
            }
        }


        try {
            categoryMapper.deleteByPrimaryKey(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> editCategory(CategoryEntity entity) {
        try {
            categoryMapper.updateByPrimaryKeySelective(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> addCategory(CategoryEntity entity) {
        try {
            categoryMapper.insertSelective(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess();
    }
}
