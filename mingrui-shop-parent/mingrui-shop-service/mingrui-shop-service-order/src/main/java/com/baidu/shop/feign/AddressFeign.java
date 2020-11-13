package com.baidu.shop.feign;

import com.baidu.shop.service.AddressService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName AddressFeign
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/28
 * @Version V1.0
 **/
@FeignClient(contextId = "AddressService",value = "site-server")
public interface AddressFeign extends AddressService {

}
