package com.baidu.shop.global;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.status.HTTPStatus;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName GolbalException
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/8/28
 * @Version V1.0
 **/
@Slf4j
@RestControllerAdvice
public class GolbalException {

    @ExceptionHandler(RuntimeException.class)
    public Result<JSONObject> test(HttpServletRequest req,Exception e){
        Result<JSONObject> result = new Result<>();
        result.setCode(HTTPStatus.ERROR);
        result.setMessage(e.getMessage());

        log.debug(e.getMessage());

        return result;
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public List<Result<JsonObject>> MethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException exception)
        throws Exception
    {
        List<Result<JsonObject>> objects = new ArrayList<>();
        for( FieldError error : exception.getBindingResult().getFieldErrors()){
            Result<JsonObject> jsonObjectResult = new Result<>();

            jsonObjectResult.setCode((HTTPStatus.PARAMS_VALIDATE_ERROR));

            jsonObjectResult.setMessage("Field-->"+ error.getField() + ":" + error.getDefaultMessage());

            log.debug("field -->" +error.getField() + ":" + error.getDefaultMessage());

            objects.add(jsonObjectResult);
        }
        return objects;
    }
}
