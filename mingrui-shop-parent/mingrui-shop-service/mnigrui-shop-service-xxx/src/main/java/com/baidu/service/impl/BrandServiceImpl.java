package com.baidu.service.impl;

import com.baidu.mapper.BrandMapper;
import com.baidu.mapper.CategoryBrandMapper;
import com.baidu.mapper.SpuMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
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
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuMapper spuMapper;


    //查询品牌信息
    @Transactional
    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        //排序
        PageHelper.startPage(brandDTO.getPage(), brandDTO.getRows());

        Example example = new Example(BrandEntity.class);

        //判断分类字段是否为空  将分类字段加入条件查询中
        if (ObjectUtil.isNotNull(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClause());

        //判断分类名称是否为空
        if (ObjectUtil.isNotNull(brandDTO.getName()))
            //创建条件查询
            example.createCriteria().andLike("name", "%" + brandDTO.getName() + "%");

        //条件查询分类
        List<BrandEntity> list = brandMapper.selectByExample(example);

        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);
    }

    //新增品牌信息
    @Transactional
    @Override
    public Result<JsonObject> saveBrand(BrandDTO brandDTO) {

        //复制实体类
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        //获取名称的第一个字体  获取字体的第一个字母 将其转换为大写
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.insertSelective(brandEntity);

        this.saveOrEdit(brandDTO, brandEntity);
        return setResultSuccess();
    }

    //修改品牌信息
    @Transactional
    @Override
    public Result<JsonObject> editBrand(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        //获取第一个字的首字母并转成大写
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //修改品牌
        brandMapper.updateByPrimaryKeySelective(brandEntity);


        //调用删除中间表方法
        this.deleteById(brandEntity.getId());

        this.saveOrEdit(brandDTO, brandEntity);
        return setResultSuccess();
    }



    //新增和修改调用的封装方法
    private void saveOrEdit(BrandDTO brandDTO, BrandEntity brandEntity) {
        if (brandDTO.getCategory().contains(",")) {
            /*String[] split = brandDTO.getCategory().split(",");

            List<String> stringList = Arrays.asList(split);

            List<CategoryBrandEntity> list = new ArrayList<>();

            stringList.stream().forEach(cid ->{
                CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
                categoryBrandEntity.setCategoryId(Integer.parseInt(cid));
                categoryBrandEntity.setBrandId(brandEntity.getId());

                list.add(categoryBrandEntity);
            });*/

            //批量新增方法
            List<CategoryBrandEntity> collect = Arrays.asList(brandDTO.getCategory().split(",")).stream().map(cid -> {
                CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
                categoryBrandEntity.setCategoryId(StringUtil.toInteger(cid));
                categoryBrandEntity.setBrandId(brandEntity.getId());
                return categoryBrandEntity;
            }).collect(Collectors.toList());
            categoryBrandMapper.insertList(collect);
        } else {
            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
            categoryBrandEntity.setBrandId(brandEntity.getId());
            categoryBrandEntity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));

            categoryBrandMapper.insertSelective(categoryBrandEntity);
        }
    }

    //删除
    @Transactional
    @Override
    public Result<JsonObject> removeBrand(Integer id) {
        //查询是否被商品绑定
        Example example = new Example(SpuEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        List<SpuEntity> spuEntities = spuMapper.selectByExample(example);
        if (spuEntities.size() >= 1) return this.setResultError("品牌被商品绑定不能被删除");

        //调用删除中间表方法
        this.deleteById(id);

        //删除商品
        brandMapper.deleteByPrimaryKey(id);

        return setResultSuccess();

    }

    //根据id查询商品分类
    @Override
    public Result<List<BrandEntity>> getBrandByCate(Integer cid) {
        List<BrandEntity> list = brandMapper.getBrandByCateId(cid);
        return this.setResultSuccess(list);
    }

    //删除品牌和分类中间表
    @Transactional
    public void deleteById(Integer id) {
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId", id);
        categoryBrandMapper.deleteByExample(example);
    }
}
