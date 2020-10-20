package com.baidu.shop.business;

import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.JwtConstant;
import com.baidu.shop.entity.UserEntity;

/**
 * @ClassName OauthService
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/15
 * @Version V1.0
 **/
public interface OauthService {

    String login(UserEntity userEntity, JwtConfig jwtConfig);
}
