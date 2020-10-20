package com.baidu.shop.feign;

import com.baidu.shop.business.SpecGroupService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xxx-service",contextId = "SpecGroupService")
public interface SpecGroupFeign extends SpecGroupService {
}
