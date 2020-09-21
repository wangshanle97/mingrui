package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @ClassName RunSearchServiceApplication
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/16
 * @Version V1.0
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableEurekaClient
@EnableFeignClients
public class RunSearchServiceApplication implements Runnable{

    public static void main(String[] args) {
        SpringApplication.run(RunSearchServiceApplication.class);
    }
    @Override
    public void run() {
        SpringApplication.run(RunSearchServiceApplication.class);
    }
}
