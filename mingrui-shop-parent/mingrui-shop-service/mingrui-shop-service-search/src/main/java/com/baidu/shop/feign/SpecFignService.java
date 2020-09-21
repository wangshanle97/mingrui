package com.baidu.shop.feign;

import com.baidu.shop.service.SpecGroupService;
import com.baidu.shop.service.SpecParamService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName SpecFignService
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/17
 * @Version V1.0
 **/
@FeignClient(contextId = "SpecGroupService",value = "xxx-service")
public interface SpecFignService extends SpecParamService {

}
