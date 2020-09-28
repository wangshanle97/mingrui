package com.baidu.fegin;

import com.baidu.shop.service.TemplateService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "template-server",contextId = "TemplateService")
public interface SpuSaveFegin extends TemplateService {
}
