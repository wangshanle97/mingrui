package com.baidu.shop.feign;

import com.baidu.shop.business.GoodsService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName GoodsFeign
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/16
 * @Version V1.0
 **/
@FeignClient(contextId = "GoodsService",value = "xxx-service")
public interface GoodsFeign extends GoodsService {

}
