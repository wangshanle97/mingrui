package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @ClassName RunCarServerApplication
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/19
 * @Version V1.0
 **/
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableEurekaClient
@EnableFeignClients
public class RunCarServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunCarServerApplication.class);
    }
}
