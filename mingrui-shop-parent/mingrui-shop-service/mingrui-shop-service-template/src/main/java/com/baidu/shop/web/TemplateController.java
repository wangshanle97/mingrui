package com.baidu.shop.web;

import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.business.PageService;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @ClassName TemplateController
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Controller
public class TemplateController {

    //@Autowired
    private PageService templateService;


    //@GetMapping("item/{spuId}.html")
    public String test(@PathVariable(value = "spuId") Integer spuId, ModelMap modelMap){
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);

        Map<String , Object> templateBySpuId = templateService.getTemplateBySpuId(spuId);
        modelMap.putAll(templateBySpuId);
        return "item";
    }

    /*@GetMapping("/a/a")
    public static String aa() {
        long val = 123;
        String s = "123";

        if (s.length() == 0) {
            return "0.00";
        }
        if (s.length() == 1) {
            System.out.println("0.0" + val);
            return "0.0" + val;
        }
        if (s.length() == 2) {
            return "0." + val;
        }
        int i = s.indexOf(".");
        if (i < 0) {
            System.out.println(s.substring(s.length()));
            System.out.println(s.substring(0, s.length() - 2) + "." + s.substring(s.length() - 2));
            return s.substring(0, s.length() - 2) + "." + s.substring(s.length() - 2);
        }
        String num = s.substring(0, i) + s.substring(i + 1);
        if (i == 1) {
            // 1位整数
            return "0.0" + num;
        }
        if (i == 2) {
            return "0." + num;
        }
        if (i > 2) {
            return num.substring(0, i - 2) + "." + num.substring(i - 2);
        }
        System.out.println(s);
        System.out.println(val);
        return null;
    }*/
}
