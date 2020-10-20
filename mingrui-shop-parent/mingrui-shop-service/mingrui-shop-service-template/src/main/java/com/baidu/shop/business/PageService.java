package com.baidu.shop.business;

import java.util.Map;

/**
 * @ClassName PageService
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/23
 * @Version V1.0
 **/
public interface PageService {


    Map<String, Object> getTemplateBySpuId(Integer spuId);
}
