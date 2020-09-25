package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.*;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TamplateServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/24
 * @Version V1.0
 **/
@RestController
public class TamplateServiceImpl extends BaseApiService implements TemplateService {

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecFeignService specFeignService;

    @Autowired
    private SpecGroupFeign specGroupFeign;

    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {


        Map<String, Object> map = this.getTemplateBySpuId(spuId);
        Context context = new Context();
        context.setVariables(map);

        File file = new File(staticHTMLPath, spuId + ".html");

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {

        Result<PageInfo<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(new SpuDTO());
        if (spuInfo.getCode() == 200){
            List<SpuDTO> spuDTOList = spuInfo.getData().getList();
            spuDTOList.stream().forEach(spu ->{
                this.createStaticHTMLTemplate(spu.getId());
            });
        }
        return this.setResultSuccess();
    }

    private Map<String, Object> getTemplateBySpuId(Integer spuId) {

        Map<String, Object> map = new HashMap<>();
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<PageInfo<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);

        if (spuInfo.getCode() == HTTPStatus.OK){
            SpuDTO spu = spuInfo.getData().getList().get(0);
            if(spuInfo.getData().getSize() == 1){
                map.put("spuInfo" , spu);

                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getData().getList().get(0).getBrandId());

                Result<PageInfo<BrandDTO>> brandInfo = brandFeign.getBrandInfo(brandDTO);

                if (brandInfo.getCode() == HTTPStatus.OK){

                    PageInfo<BrandDTO> data = brandInfo.getData();
                    List<BrandDTO> brandDTOList = data.getList();

                    if (brandDTOList.size() == 1){
                        brandDTO.getName();
                        map.put("brandInfo",brandDTOList.get(0));
                    }
                }
                Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBySpuId(spuId);
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
                Result<List<SkuDTO>> skuBySpuId = goodsFeign.getSkuBySpuId(spuId);
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
