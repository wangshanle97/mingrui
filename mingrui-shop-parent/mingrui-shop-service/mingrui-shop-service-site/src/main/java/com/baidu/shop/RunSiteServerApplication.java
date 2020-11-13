package com.baidu.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @ClassName RunSiteServerApplication
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/27
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.baidu.shop.mapper")
public class RunSiteServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(RunSiteServerApplication.class);
    }
}
