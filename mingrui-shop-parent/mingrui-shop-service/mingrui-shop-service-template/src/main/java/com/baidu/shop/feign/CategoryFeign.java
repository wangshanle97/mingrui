package com.baidu.shop.feign;

import com.baidu.shop.business.CategoryService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xxx-service",contextId = "CategoryService")
public interface CategoryFeign extends CategoryService {
}
