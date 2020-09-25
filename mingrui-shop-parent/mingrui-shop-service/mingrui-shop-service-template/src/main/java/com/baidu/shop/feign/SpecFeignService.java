package com.baidu.shop.feign;

import com.baidu.shop.service.SpecParamService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName SpecFeignService
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/23
 * @Version V1.0
 **/
@FeignClient(value = "xxx-service",contextId = "SpecParamService")
public interface SpecFeignService extends SpecParamService {

}
