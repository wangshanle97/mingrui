package com.baidu;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @ClassName com.baidu.RunTestApplication
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/16
 * @Version V1.0
 **/
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RunTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunTestApplication.class);
    }

    @Bean
    public Redisson redisson(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://119.45.206.101:6379").setDatabase(0);
        return (Redisson) Redisson.create(config);
    }
}
