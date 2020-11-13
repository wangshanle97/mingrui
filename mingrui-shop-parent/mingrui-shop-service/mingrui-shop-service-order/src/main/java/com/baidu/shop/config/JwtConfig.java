package com.baidu.shop.config;

import com.baidu.shop.utils.RsaUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @ClassName JwtConfig
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/21
 * @Version V1.0
 **/
@Data
@Configuration
public class JwtConfig {

    @Value("${mrshop.jwt.pubKeyPath}")
    private String pubKeyPath;

    @Value("${mrshop.jwt.cookieName}")
    private String cookieName;

    private PublicKey publicKey;

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @PostConstruct
    public void init(){

        try {
            this.publicKey = RsaUtil.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            logger.error("获取公钥失败");
            throw new RuntimeException();
        }
    }
}
