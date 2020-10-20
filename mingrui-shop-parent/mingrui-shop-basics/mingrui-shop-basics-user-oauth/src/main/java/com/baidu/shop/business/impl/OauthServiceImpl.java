package com.baidu.shop.business.impl;

import com.baidu.shop.business.OauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.JwtConstant;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserOauthMapper;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName OauthServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Service
public class OauthServiceImpl implements OauthService {

    @Resource
    private UserOauthMapper userOauthMapper;


    @Override
    public String login(UserEntity userEntity, JwtConfig jwtConfig) {

        String token = null;
        Example example = new Example(UserEntity.class);
        example.createCriteria().andEqualTo("username",userEntity.getUsername());
        List<UserEntity> list = userOauthMapper.selectByExample(example);

        if (list.size() == 1 ){
            UserEntity entity = list.get(0);
            if (BCryptUtil.checkpw(userEntity.getPassword(),entity.getPassword())) {

                try {
                    token = JwtUtil.generateToken(new UserInfo(entity.getId(),entity.getUsername()),jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return token;
    }
}
