package com.baidu.shop.feign;

import com.baidu.shop.business.GoodsService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xxx-service",contextId="GoodsService")
public interface GoodsFeign extends GoodsService {
}
