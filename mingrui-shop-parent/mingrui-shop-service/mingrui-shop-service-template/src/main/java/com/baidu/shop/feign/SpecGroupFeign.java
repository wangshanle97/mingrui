package com.baidu.shop.feign;

import com.baidu.shop.service.SpecGroupService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xxx-service",contextId = "SpecGroupService")
public interface SpecGroupFeign extends SpecGroupService {
}
