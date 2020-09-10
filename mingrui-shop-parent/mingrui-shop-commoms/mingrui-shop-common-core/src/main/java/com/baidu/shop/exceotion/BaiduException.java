package com.baidu.shop.exceotion;

import com.baidu.shop.status.HTTPStatus;

/**
 * @ClassName BaiduException
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/9/3
 * @Version V1.0
 **/
public class BaiduException extends Throwable {

    private String msg;

    private Integer code;

    public BaiduException(String msg, Integer code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

}
