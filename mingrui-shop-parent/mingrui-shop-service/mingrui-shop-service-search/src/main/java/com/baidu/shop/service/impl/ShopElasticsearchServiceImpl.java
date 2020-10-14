package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SearchDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.service.SpecParamService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchService
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
@Slf4j
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecParamService specParamService;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private BrandFeign brandFeign;

    @Override
    public Result<JSONObject> saveData(Integer spuId) {

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        List<GoodsDoc> goodsDocs = this.getGoods(spuDTO);
        GoodsDoc goodsDoc = goodsDocs.get(0);
        elasticsearchRestTemplate.save(goodsDoc);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delData(Integer spuId) {
        GoodsDoc goodsDoc = new GoodsDoc();
        goodsDoc.setId(spuId.longValue());
        elasticsearchRestTemplate.delete(goodsDoc);
        return this.setResultSuccess();
    }

    // 搜索主方法
    @Override
    public GoodsResponse search(SearchDTO searchDTO) {

        // 判断搜索框是否为空
        if (StringUtil.isEmpty(searchDTO.getSearch())) throw new RuntimeException("搜索框不能为空");
        //将查询条件 和分页信息 放到查询里面去查询
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate
                .search(this.getPageAndSearchAndCidAndBrandId(searchDTO.getSearch(),searchDTO.getPage(),searchDTO.getFilter()).build(), GoodsDoc.class);
        //调用高亮封装类 设置高亮
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());
        // 遍历查询出来的高亮信息数据用map集合遍历放到list集合中
        List<GoodsDoc> list = highLightHit.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());

        //总条数&总页数
        long total = searchHits.getTotalHits();
        // 将总条数转换为long类型 在转换为double类型 调用向上取整函数 除10 获得总页数 再次转换为long类型
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        Aggregations aggregations = searchHits.getAggregations();
        //调用分类查询方法
        Map<Integer, List<CategoryEntity>> map = this.getCategoryIdList(aggregations);
        List<CategoryEntity> categoryList = null;
        Integer hotCid = 0;

        for (Map.Entry<Integer,List<CategoryEntity>> mapEntry : map.entrySet()){
            hotCid = mapEntry.getKey();
            categoryList = mapEntry.getValue();
        }
        Map<String, List<String>> specParam  = this.getSpecParam(hotCid, searchDTO.getSearch());

        // 将 总条数&总页数&品牌信息&分类信息&高亮集合 放到 封装的response里面返回
        return new GoodsResponse(total, totalPage, this.getBrandIdList(aggregations).getData(), categoryList, list,specParam);
    }

    private  Map<String, List<String>> getSpecParam(Integer hotCid,String search){

        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setSearching(true);
        specParamDTO.setCid(hotCid);
        Result<List<SpecParamEntity>> specParamResult = specParamService.getSpecParamInfo(specParamDTO);
        if (specParamResult.getCode() == 200) {
            List<SpecParamEntity> infoData = specParamResult.getData();
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

            builder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));
            //分页必须得查询一条数据
            builder.withPageable(PageRequest.of(0,1));

            infoData.stream().forEach(specParam -> {
                builder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs." + specParam.getName() + ".keyword"));
            });

            SearchHits<GoodsDoc> docSearchHits = elasticsearchRestTemplate.search(builder.build(), GoodsDoc.class);
            Map<String, List<String>> map = new HashMap<>();
            Aggregations aggregations = docSearchHits.getAggregations();
            infoData.stream().forEach(specParam -> {

                Terms terms = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();

                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

                map.put(specParam.getName(),valueList);
            });
            return map;
        }
        return null;
    }

    /**
     * 查询search和设置分页
     * @param search
     * @param page
     * @return
     */
    private NativeSearchQueryBuilder getPageAndSearchAndCidAndBrandId(String search,Integer page,String filter){

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //filter 两个{} 也是字符 所以 要判断filter是否为空 长度是否大于2
        if (StringUtil.isNotEmpty(filter) && filter.length() >2 ){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //将filter字符串转换为json类型
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);
            //循环filter获取到key值和value值
            filterMap.forEach((key,value) ->{
                //定义一个自定义查询
                MatchQueryBuilder matchQueryBuilder = null;
                //去过key是商品id或者是品牌ID
                if (key.equals("cid3") || key.equals("brandId")){
                    //
                    matchQueryBuilder = QueryBuilders.matchQuery(key, value);
                }else{
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + key + ".keyword", value);
                }
                boolQueryBuilder.must(matchQueryBuilder);
            });
            nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        }
        //多条件查询  条件为品牌名称 分类名称 标题
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));
        //分页 每页十条
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page-1,10));
        //设置嵌套查询 根据ID查询分类 和查询品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("cidAgg").field("cid3"));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brandIdAgg").field("brandId"));

        //设置高亮字段 将标题设置为高亮
        nativeSearchQueryBuilder.withHighlightBuilder(ESHighLightUtil.getHighBrightBuilder("title"));
        return nativeSearchQueryBuilder;
    }

    /**
     * 查询分类信息
     * @param aggregations
     * @return
     */
    private  Map<Integer, List<CategoryEntity>> getCategoryIdList(Aggregations aggregations){
        Terms cidAgg = aggregations.get("cidAgg");
        List<? extends Terms.Bucket> buckets = cidAgg.getBuckets();

        List<Integer> hotCidArr = Arrays.asList(0);
        List<Long> maxCount = Arrays.asList(0L);
        //通过ID集合查询分类信息
        //List<? extends Terms.Bucket> cidAggBuckets = cidAgg.getBuckets();
        //将查询出来的分类信息用map遍历 转换成int类型的字符串 放到list集合里
        ArrayList<Object> objects = new ArrayList<>();
        HashMap<Integer, List<CategoryEntity>> map = new HashMap<>();
        List<String> cidStrList = buckets.stream().map(cidList -> {
            if (cidList.getDocCount() > maxCount.get(0)){
                maxCount.set(0,cidList.getDocCount());
                hotCidArr.set(0,cidList.getKeyAsNumber().intValue());
            }
            return cidList.getKeyAsNumber().intValue() + ",";
        }).collect(Collectors.toList());

        // 将分类集合通过逗号隔开转换为字符串
        String cidStr = String.join(",", cidStrList);
        map.put(hotCidArr.get(0),categoryFeign.getCategoryByIdList(cidStr).getData());
        return map;
    }

    /**
     * 通过品牌id集合查询品牌信息
     * @param aggregations
     * @return
     */
    private Result<List<BrandEntity>> getBrandIdList(Aggregations aggregations){
        Terms brandIdAgg = aggregations.get("brandIdAgg");
        //List<? extends Terms.Bucket> brandIdAggBuckets = brandIdAgg.getBuckets();
        //遍历查询出来的数据
        List<String> brandIdStrList = brandIdAgg.getBuckets().stream().map(
                brandIdList -> brandIdList.getKeyAsString()).collect(Collectors.toList());
        //将list集合转换为字符串 用逗号隔开
        String brandIdStr = String.join(",",brandIdStrList);

        return brandFeign.getBrandByIdList(brandIdStr);
    }

    //初始化es库
    @Override
    public Result<JSONObject> initGoodsEsData() {

        /*IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        //判断是否创建索引和mapping是否成功
        if (indexOperations.exists()){
            indexOperations.create();
            indexOperations.createMapping();
        }*/
        //调用查询方法

        List<GoodsDoc> goods = this.getGoods(new SpuDTO());
        elasticsearchRestTemplate.save(goods);
        return this.setResultSuccess();
    }

    // 删除es库
    @Override
    public Result<JSONObject> clearGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(indexOperations.exists()){
            indexOperations.delete();
            log.info("索引删除成功");
        }

        return this.setResultSuccess();
    }

    private  List<GoodsDoc> getGoods(SpuDTO spuDTO) {

//        spuDTO.setRows(5);
//        spuDTO.setPage(1);
        Result<PageInfo<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);

        List<SpuDTO> data = spuInfo.getData().getList();

        List<GoodsDoc> goodsDocs = new ArrayList<>();

        if (spuInfo.getCode() == HTTPStatus.OK){

            data.stream().forEach(spu ->{

                GoodsDoc goodsDoc = new GoodsDoc();
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setBrandId(spu.getBrandId().longValue());

                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setTitle(spu.getTitle());


                Map<List<Long>, List<Map<String, Object>>> skuList = this.getSkuList(spu.getId());
                skuList.forEach((key,value)->{
                    goodsDoc.setPrice(key);
                    goodsDoc.setSkus(JSONUtil.toJsonString(value));
                });

                Map<String, Object> paramSpec = this.getParamSpec(spu.getCid3(),spu.getId());

                goodsDoc.setSpecs(paramSpec);
                goodsDocs.add(goodsDoc);

            });
        }
        return goodsDocs;
    }

    private Map<List<Long>, List<Map<String, Object>>> getSkuList(Integer spuid){

        Map<List<Long>, List<Map<String, Object>>> hashMap = new HashMap<>();

        Result<List<SkuDTO>> skuBySpuId = goodsFeign.getSkuBySpuId(spuid);

        List<Long> priceList = new ArrayList<>();

        List<SkuDTO> skuList = skuBySpuId.getData();

        List<Map<String, Object>> skuMap = null;

        if (skuBySpuId.getCode() == HTTPStatus.OK){

            skuMap = skuList.stream().map(sku -> {

                HashMap<String, Object> map = new HashMap<>();
                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("images", sku.getImages());
                map.put("price", sku.getPrice());

                priceList.add(sku.getPrice().longValue());

                return map;
            }).collect(Collectors.toList());
        }
        hashMap.put(priceList,skuMap);
        return hashMap;
    }

    private Map<String,Object> getParamSpec(Integer spuCid ,Integer spuId){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuCid);
        Result<List<SpecParamEntity>> specParamInfo = specParamService.getSpecParamInfo(specParamDTO);

        Map<String, Object> specMap = new HashMap<>();
        if (specParamInfo.getCode() == HTTPStatus.OK){

            List<SpecParamEntity> paramList = specParamInfo.getData();

            Result<SpuDetailEntity> spuDetailBySpuId = goodsFeign.getSpuDetailBySpuId(spuId);
            if (spuDetailBySpuId.getCode() == HTTPStatus.OK){
                SpuDetailEntity spuDetailList = spuDetailBySpuId.getData();

                Map<String, List<String>> specialSpecMap = JSONUtil.toMapValueStrList(spuDetailList.getSpecialSpec());
                Map<String, String> genericSpecMap = JSONUtil.toMapValueString(spuDetailList.getGenericSpec());

                paramList.stream().forEach(param ->{
                    if (param.getGeneric()){
                        if (param.getNumeric() && param.getSearching()){
                            specMap.put(param.getName(), this.chooseSegment(genericSpecMap.get(param.getId() + ""),param.getSegments(),param.getUnit()));
                        }else{
                            specMap.put(param.getName(),genericSpecMap.get(param.getId() + ""));
                        }
                    }else{
                        specMap.put(param.getName(),specialSpecMap.get(param.getId() + ""));
                    }
                });

            }
        }
        return specMap;
    }

    /**
     * 把具体的值转换成区间-->不做范围查询
     * @param value
     * @param segments
     * @param unit
     * @return
     */
    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }

}
