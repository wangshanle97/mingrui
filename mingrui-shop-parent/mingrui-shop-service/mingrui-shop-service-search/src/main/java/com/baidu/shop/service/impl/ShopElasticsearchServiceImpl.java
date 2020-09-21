package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
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
import com.baidu.shop.service.SpecParamService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName ShopElasticsearchService
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
public class ShopElasticsearchServiceImpl extends BaseApiService implements com.baidu.shop.service.ShopElasticsearchService {

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
    public GoodsResponse search(String search,Integer page) {
        if (StringUtil.isEmpty(search)) throw new RuntimeException("搜索框不能为空");

        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate
                .search(this.getPageAndSearchAndCidAndBrandId(search,page).build(), GoodsDoc.class);

        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());

        List<GoodsDoc> list = highLightHit.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());

        //总条数&总页数
        long total = searchHits.getTotalHits();
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        Aggregations aggregations = searchHits.getAggregations();

        Result<List<CategoryEntity>> categoryResult = this.getCategoryIdList(aggregations);
        GoodsResponse goodsResponse = new GoodsResponse(total, totalPage, this.getBrandIdList(aggregations).getData(), categoryResult.getData(), list);

        return goodsResponse;
    }

    /**
     * 查询search和分页
     * @param search
     * @param page
     * @return
     */
    private NativeSearchQueryBuilder getPageAndSearchAndCidAndBrandId(String search,Integer page){

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //条件查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));
        //分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page-1,10));

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("cidAgg").field("cid3"));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brandIdAgg").field("brandId"));

        //设置高亮字段
        nativeSearchQueryBuilder.withHighlightBuilder(ESHighLightUtil.getHighBrightBuilder("title"));
        return nativeSearchQueryBuilder;
    }

    private  Result<List<CategoryEntity>> getCategoryIdList(Aggregations aggregations){
        Terms cidAgg = aggregations.get("cidAgg");

        List<? extends Terms.Bucket> cidAggBuckets = cidAgg.getBuckets();

        List<String> cidStrList = cidAggBuckets.stream().map(cidList -> {
            Number keyAsNumber = cidList.getKeyAsNumber();
            return keyAsNumber.intValue() + ",";
        }).collect(Collectors.toList());

        String cidStr = String.join(",", cidStrList);
        return categoryFeign.getCategoryByIdList(cidStr);

    }

    private Result<List<BrandEntity>> getBrandIdList(Aggregations aggregations){
        Terms brandIdAgg = aggregations.get("brandIdAgg");
        List<? extends Terms.Bucket> brandIdAggBuckets = brandIdAgg.getBuckets();

        List<String> brandIdStrList = brandIdAggBuckets.stream().map(brandIdList -> brandIdList.getKeyAsString()).collect(Collectors.toList());

        String brandIdStr = String.join(",",brandIdStrList);

        return brandFeign.getBrandByIdList(brandIdStr);
    }

    @Override
    public Result<JSONObject> initGoodsEsData() {

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (indexOperations.exists()){
            indexOperations.create();
            indexOperations.createMapping();
        }
        List<GoodsDoc> goods = this.goods();
        elasticsearchRestTemplate.save(goods);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> clearGoodsEsData() {

        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);

        if (indexOperations.exists()){
            indexOperations.delete();
        }

        return this.setResultSuccess();
    }

    private  List<GoodsDoc> goods() {
        SpuDTO spuDTO = new SpuDTO();
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




                Map<String, Object> paramSpec = this.getParamSpec(spu.getCid3());

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

    private Map<String,Object> getParamSpec(Integer spuCid){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuCid);
        Result<List<SpecParamEntity>> specParamInfo = specParamService.getSpecParamInfo(specParamDTO);

        Map<String, Object> specMap = new HashMap<>();
        if (specParamInfo.getCode() == HTTPStatus.OK){

            List<SpecParamEntity> paramList = specParamInfo.getData();

            Result<SpuDetailEntity> spuDetailBySpuId = goodsFeign.getSpuDetailBySpuId(spuCid);
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
