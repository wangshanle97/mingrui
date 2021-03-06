package com.baidu.service.impl;

import com.baidu.component.MrRabbitmq;
import com.baidu.shop.constant.MrMessageConstant;
import com.baidu.mapper.*;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.dto.StockDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.business.GoodsService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName SpuServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Autowired
    private MrRabbitmq mrRabbitmq;

//    @Autowired
//    private SpuSaveFeign spuSaveFeign;

//    @Autowired
//    private ShopElasticsearchFeign shopElasticsearchFeign;

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    @Override
    public Result<JsonObject> updateStock(StockDTO stockDTO) {

        stockMapper.updateStockList(stockDTO.getSkuId(),stockDTO.getStock());
        return this.setResultSuccess();
    }

    @Override
    public Result<SkuEntity> getSkuBySkuId(Long skuId) {
        SkuEntity skuEntity = skuMapper.selectByPrimaryKey(skuId);
        return this.setResultSuccess(skuEntity);
    }

    @Override
    public Result<PageInfo<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {

        //判断page不为空 分页
        if(ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        // List<SpuDTO> spuOrGorupList = spuMapper.getSpuOrGorupList(spuDTO);
        //构建条件查询
        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();

        //判断id是否为空
        if (ObjectUtil.isNotNull(spuDTO.getId())) criteria.andEqualTo("id",spuDTO.getId());
        //通过标题模糊查询
        if(StringUtil.isNotEmpty(spuDTO.getTitle()))
            criteria.andLike("title","%"+spuDTO.getTitle()+"%");

        //判断是否上架 不等于2 就放到条件查询中
        if(ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)
            criteria.andEqualTo("saleable",spuDTO.getSaleable());

        //排序
        if(StringUtil.isNotEmpty(spuDTO.getSort())) example.setOrderByClause(spuDTO.getOrderByClause());

        List<SpuEntity> list = spuMapper.selectByExample(example);

        //调用封装方法
        List<SpuDTO> spuDTOList = this.queryBrandByCategoryList(list);

        PageInfo<SpuDTO> pageInfo = new PageInfo<>(spuDTOList);
        return this.setResultSuccess(pageInfo);

        //return this.setResultSuccess(new PageInfo<>(spuOrGorupList));
    }

    //被查询商品调用的方法
    public List<SpuDTO> queryBrandByCategoryList(List<SpuEntity> list){

            //通过品牌id查询品牌名称
            List<SpuDTO> spuDtoList = list.stream().map(spuEntity -> {
                SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);

                BrandEntity brandEntity = brandMapper.selectByPrimaryKey(spuEntity.getBrandId());

                if( ObjectUtil.isNotNull(brandEntity))  spuDTO1.setBrandName(brandEntity.getName());
                //设置分类
                //通过cid1 cid2 cid3
    //            List<CategoryEntity> categoryEntityList = categoryMapper.selectByIdList(Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()));
    //            String  categoryName  = categoryEntityList.stream().
    //                    map(category -> category.getName()).collect(Collectors.joining("/"));
                //拼接查询
                String  categoryName  = categoryMapper.selectByIdList(
                        //获取 cid1/2/3
                        Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()))
                        //遍历类目名称
                        .stream().map(category -> category.getName())
                        //拼接类目
                        .collect(Collectors.joining("/"));

                spuDTO1.setCategoryName(categoryName);

                return spuDTO1;
            }).collect(Collectors.toList());
        return spuDtoList;
    }

    //
    @Override
    public Result<JsonObject> saveSpu(SpuDTO spuDTO) {

        Integer info = this.addInfo(spuDTO);


//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
//            @Override
//            public void afterCommit() {
//                spuSaveFeign.createStaticHTMLTemplate(spuid);
//
//                shopElasticsearchFeign.initGoodsEsData(spuid);
//            }
//        });

        mrRabbitmq.send(info + "", MrMessageConstant.SPU_ROUT_KEY_SAVE);

        return this.setResultSuccess();
    }

    @Transactional
    public Integer addInfo(SpuDTO spuDTO){
        //new 一个当前时间
        Date date = new Date();

        //复制spuEntity实体
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);

        spuMapper.insertSelective(spuEntity);

        Integer spuid = spuEntity.getId();

        //新增 spudetail表 商品描述信息
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);

        spuDetailEntity.setSpuId(spuid);
        spuDetailMapper.insertSelective(spuDetailEntity);
        System.out.println(spuDTO.getSkus().get(0).getPrice());
        //新增sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8
        //if (spuDTO.getSkus().get(0).getPrice() == 0);
        this.saveSkuAndStock(spuDTO.getSkus(),spuid,date);

        return spuid;
    }

    //查询spu 商品信息
    @Override
    public Result<SpuDetailEntity> getSpuDetailBySpuId(Integer spuid) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuid);
        return this.setResultSuccess(spuDetailEntity);
    }

    //查询sku 实体信息
    @Override
    public Result<List<SkuDTO> > getSkuBySpuId(Integer spuid) {
        List<SkuDTO> skuDTOList = skuMapper.selectSkuAndStockBySpuId(spuid);
        return this.setResultSuccess(skuDTOList);
    }


    //修改商品信息
    //Transactional
    @Override
    public Result<SkuDTO> editSpu(SpuDTO spuDTO) {

        this.updateInfoTransactional(spuDTO);
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
//            @Override
//            public void afterCommit() {
//
//                shopElasticsearchFeign.initGoodsEsData(spuDTO.getId());
//                File file = new File("E:\\static-html\\item\\" + spuDTO.getId() + ".html");
//                if (file.exists()){
//                    file.delete();
//                }
//                spuSaveFeign.createStaticHTMLTemplate(spuDTO.getId());
//            }
//        });


        mrRabbitmq.send(spuDTO.getId() + "", MrMessageConstant.SPU_ROUT_KEY_UPDATE);


        return this.setResultSuccess();
    }

    @Transactional
    public void updateInfoTransactional(SpuDTO spuDTO){
        //new 当前系统时间
        Date date = new Date();

        //赋值商品实体
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        //修改商品信息
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        //修改商品描述信息
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuDTO.getId());
        spuDetailMapper.updateByPrimaryKeySelective(spuDetailEntity);

        List<Long> collect = this.querySkuIdBySpuId(spuDTO.getId());

        if(collect.size() > 0){
            //删除商品实体信息
            skuMapper.deleteByIdList(collect);

            //删除仓库表信息
            stockMapper.deleteByIdList(collect);
        }
        //调用批量新增商品实体 & 商品库存
        this.saveSkuAndStock(spuDTO.getSkus(),spuDTO.getId(),date);
    }

    //删除商品信息方法

    //@Transactional
    @Override
    public Result<JsonObject> delSpu(Integer spuId) {
        this.delInfoTransactional(spuId);

//        File file = new File("E:\\static-html\\item\\" + spuId + ".html");
//        if (file.exists()){
//            file.delete();
//            System.out.println("删除成功");
//        }
//        shopElasticsearchFeign.clearGoodsEsData(spuId.toString());
        mrRabbitmq.send(spuId + "", MrMessageConstant.SPU_ROUT_KEY_DELETE);

        return this.setResultSuccess();
    }

    @Transactional
    public void delInfoTransactional(Integer spuId){
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        List<Long> list = skuEntities.stream().map(sku -> sku.getId() ).collect(Collectors.toList());

        //删除商品实体
        spuMapper.deleteByPrimaryKey(spuId);

        //删除商品参数信息
        spuDetailMapper.deleteByPrimaryKey(spuId);

        if (list.size() != 0){
            stockMapper.deleteByIdList(list);
            skuMapper.deleteByIdList(list);
        }
    }

    //被调用的查询方法
    private List<Long> querySkuIdBySpuId(Integer spuId){
        //构建条件查询
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);

        //查询商品实体信息 返回一个集合
        List<SkuEntity> skuEntityList = skuMapper.selectByExample(example);
        //遍历集合 获得商品id
        List<Long> collect = skuEntityList.stream().map(sku -> sku.getId()).collect(Collectors.toList());
        return collect;
    }

    //被调用的新增方法
    @Transactional
    public void saveSkuAndStock(List<SkuDTO> skus,Integer spuid,Date date){
        skus.stream().forEach(skuDTO -> {
            //新增 商品实体,如黑色的 64g的iphone 8
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuid);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);

            skuMapper.insertSelective(skuEntity);

            //新增 stock 库存表，代表库存，秒杀库存等信息
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            //if (skuDTO.getPrice() == )

            stockMapper.insertSelective(stockEntity);
        });
    }

    @Override
    public Result<JsonObject> editDownOrUp(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setId(spuDTO.getId());
        if(spuEntity.getSaleable() == 1){
            spuEntity.setSaleable(0);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("下架成功");
        }else{
            spuEntity.setSaleable(1);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("上架成功");
        }
    }


}
