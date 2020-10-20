package com.baidu.shop.business.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.*;
import com.baidu.shop.business.PageService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/23
 * @Version V1.0
 **/
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private GoodsFeign goodsService;

    @Autowired
    private BrandFeign brandService;

    @Autowired
    private SpecFeignService specFeignService;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SpecGroupFeign specGroupFeign;


    @Override
    public Map<String, Object> getTemplateBySpuId(Integer spuId) {

        Map<String, Object> map = new HashMap<>();
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<PageInfo<SpuDTO>> spuInfo = goodsService.getSpuInfo(spuDTO);

        if (spuInfo.getCode() == HTTPStatus.OK){
            SpuDTO spu = spuInfo.getData().getList().get(0);
            if(spuInfo.getData().getSize() == 1){
                map.put("spuInfo" , spu);

                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getData().getList().get(0).getBrandId());

                Result<PageInfo<BrandDTO>> brandInfo = brandService.getBrandInfo(brandDTO);

                if (brandInfo.getCode() == HTTPStatus.OK){

                    PageInfo<BrandDTO> data = brandInfo.getData();
                    List<BrandDTO> brandDTOList = data.getList();

                    if (brandDTOList.size() == 1){
                        brandDTO.getName();
                        map.put("brandInfo",brandDTOList.get(0));
                    }
                }
                Result<SpuDetailEntity> spuDetailResult = goodsService.getSpuDetailBySpuId(spuId);
                if (spuDetailResult.getCode() == HTTPStatus.OK){
                    map.put("spuDetailList",spuDetailResult.getData());
                }

                //查询规格组
            SpecGroupDTO specGroupDTO = new SpecGroupDTO();
            specGroupDTO.setCid(spuInfo.getData().getList().get(0).getCid3());
            Result<List<SpecGroupEntity>> sepcGroupInfo = specGroupFeign.getSepcGroupInfo(specGroupDTO);

            if (sepcGroupInfo.getCode() == HTTPStatus.OK){

                List<SpecGroupDTO> groupDTOList = sepcGroupInfo.getData().stream().map(specGroup -> {

                    SpecGroupDTO specGroupDTO1 = BaiduBeanUtil.copyProperties(specGroup, SpecGroupDTO.class);

                    SpecParamDTO specParamDTO = new SpecParamDTO();
                    specParamDTO.setGroupId(specGroupDTO1.getId());
                    specParamDTO.setGeneric(true);

                    Result<List<SpecParamEntity>> specParamInfo = specFeignService.getSpecParamInfo(specParamDTO);

                    if (specParamInfo.getCode() == 200) {
                        specGroupDTO1.setSpecParam(specParamInfo.getData());
                    }
                    return specGroupDTO1;
                }).collect(Collectors.toList());
                map.put("groupDTOList",groupDTOList);

            }

                // 查询规格参数
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getData().getList().get(0).getCid3());
                specParamDTO.setGeneric(false);
                Result<List<SpecParamEntity>> specParamInfo = specFeignService.getSpecParamInfo(specParamDTO);

                if (specParamInfo.getCode() == HTTPStatus.OK){
                    Map<Integer, String> specMap = new HashMap<>();
                    specParamInfo.getData().stream().forEach(spec ->{
                        specMap.put(spec.getId(),spec.getName());
                        map.put("specParamMap",specMap);
                    });
                }

                //分类信息
                Result<List<SkuDTO>> skuBySpuId = goodsService.getSkuBySpuId(spuId);
                if (skuBySpuId.getCode() == HTTPStatus.OK){
                    List<SkuDTO> data = skuBySpuId.getData();
                    map.put("skuList",data);
                }

                // 品牌分类
                Result<List<CategoryEntity>> cateResult = categoryFeign.getCategoryByIdList(String.join(","
                        , Arrays.asList(spuInfo.getData().getList().get(0).getCid1() + ""
                                , spuInfo.getData().getList().get(0).getCid2() + ""
                                , spuInfo.getData().getList().get(0).getCid3() + ""
                        )));
                if (cateResult.getCode() == HTTPStatus.OK){
                    map.put("cateList",cateResult.getData());
                }
            }
        }
        return map;
    }
}
