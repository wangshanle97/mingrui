package com.baidu.service.impl;

import com.baidu.mapper.BrandMapper;
import com.baidu.mapper.CategoryBrandMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService{

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        Example example = new Example(BrandEntity.class);

        if (ObjectUtil.isNotNull(brandDTO.getSort()))example.setOrderByClause(brandDTO.getOrderByClause());

        if (ObjectUtil.isNotNull(brandDTO.getName())) example.createCriteria().andLike("name","%"+brandDTO.getName()+"%");


        List<BrandEntity> list = brandMapper.selectByExample(example);

        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);
    }

    @Override
    public Result<JsonObject> saveBrand(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.insertSelective(brandEntity);

        this.saveOrEdit(brandDTO,brandEntity);
        return setResultSuccess();
    }

    @Override
    public Result<JsonObject> editBrand(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.updateByPrimaryKeySelective(brandEntity);


        this.deleteById(brandEntity.getId());

        this.saveOrEdit(brandDTO,brandEntity);
        return setResultSuccess();
    }


    private void saveOrEdit(BrandDTO brandDTO,BrandEntity brandEntity){
        if(brandDTO.getCategory().contains(",")){
            /*String[] split = brandDTO.getCategory().split(",");

            List<String> stringList = Arrays.asList(split);

            List<CategoryBrandEntity> list = new ArrayList<>();

            stringList.stream().forEach(cid ->{
                CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
                categoryBrandEntity.setCategoryId(Integer.parseInt(cid));
                categoryBrandEntity.setBrandId(brandEntity.getId());

                list.add(categoryBrandEntity);
            });*/

            List<CategoryBrandEntity> collect = Arrays.asList(brandDTO.getCategory().split(",")).stream().map(cid -> {
                CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
                categoryBrandEntity.setCategoryId(StringUtil.toInteger(cid));
                categoryBrandEntity.setBrandId(brandEntity.getId());
                return categoryBrandEntity;
            }).collect(Collectors.toList());
            categoryBrandMapper.insertList(collect);
        }else{
            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
            categoryBrandEntity.setBrandId(brandEntity.getId());
            categoryBrandEntity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));

            categoryBrandMapper.insertSelective(categoryBrandEntity);
        }
    }

    @Override
    public Result<JsonObject> removeBrand(Integer id) {

        this.deleteById(id);

        brandMapper.deleteByPrimaryKey(id);
        return setResultSuccess();

    }

    private void deleteById(Integer id){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);
    }
}
