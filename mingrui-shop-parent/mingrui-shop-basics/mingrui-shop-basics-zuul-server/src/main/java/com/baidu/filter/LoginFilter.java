package com.baidu.filter;

import com.baidu.config.JwtConfig;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName LoginFilter
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/16
 * @Version V1.0
 **/
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtConfig jwtConfig;

    private static final Logger logger =  LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public String filterType() {

        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 5;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext currentContext = RequestContext.getCurrentContext();

        HttpServletRequest request = currentContext.getRequest();

        String requestURI = request.getRequestURI();
        logger.debug("----------------"+requestURI);

        Boolean flag = true;
        for(String s : jwtConfig.getExcludesPath()){
            flag = !(requestURI.indexOf(s) != -1);
            if(!flag) break;
        }
        return flag;
        //return !jwtConfig.getExcludesPath().contains(requestURI );
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        logger.info("拦截到请求"+request.getRequestURI());

        String token = CookieUtils.getCookieValue(request, jwtConfig.getCookieName());
        logger.info("token信息"+token);

        try {
            JwtUtil.getInfoFromToken(token,jwtConfig.getPublicKey());
        } catch (Exception e) {
            logger.info("解析失败,拦截"+token);

            currentContext.setSendZuulResponse(false);
            currentContext.setResponseStatusCode(403);
        }

        return null;
    }
}
