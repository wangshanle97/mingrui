package com.baidu.shop.web;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.JwtConstant;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName UserOauthController
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/15
 * @Version V1.0
 **/
@RestController
@Api(tags = "用户登录验证")
public class UserOauthController extends BaseApiService {

    @Autowired
    private OauthService oauthService;

    @Autowired
    private JwtConfig jwtConfig;

    @ApiOperation(value = "用户登录")
    @PostMapping("oauth/login")
    public Result<JsonObject> login(@RequestBody UserEntity userEntity, HttpServletRequest httpServletRequest
            , HttpServletResponse httpServletResponse){

        String token = oauthService.login(userEntity,jwtConfig);

        if (ObjectUtil.isNull(token)) {
            return this.setResultError(HTTPStatus.VALID_USER_PASSWORD_ERROR,"用户名或密码错误");
        }
        CookieUtils.setCookie(httpServletRequest,httpServletResponse
                ,jwtConfig.getCookieName(),token,jwtConfig.getCookieMaxAge(),true);

        return this.setResultSuccess();
    }

    @GetMapping(value = "oauth/verify")
    public Result<UserInfo> checkUserIsLogin(@CookieValue(value = "MRSHOP_TOKEN") String token
            ,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){

        if (StringUtil.isNotEmpty(token)){
            try {
                UserInfo userInfo = JwtUtil.getInfoFromToken(token, jwtConfig.getPublicKey());

                String s = JwtUtil.generateToken(userInfo, jwtConfig.getPrivateKey(), jwtConfig.getExpire());

                CookieUtils.setCookie(httpServletRequest,httpServletResponse,jwtConfig.getCookieName(),
                        s,jwtConfig.getCookieMaxAge(),true);
                return this.setResultSuccess(userInfo);
            } catch (Exception e) {//如果有异常 说明token有问题
                //e.printStackTrace();
                //应该新建http状态为用户验证失败,状态码为403
                return this.setResultError(HTTPStatus.VERIFY_ERROR,"用户失效");
            }
        }else{
            System.out.println("未登录");
        }
        return this.setResultError("未登录");
    }

}
