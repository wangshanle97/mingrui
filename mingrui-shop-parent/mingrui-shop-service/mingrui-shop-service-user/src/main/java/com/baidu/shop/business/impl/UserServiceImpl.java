package com.baidu.shop.business.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.RegisterConstant;
import com.baidu.shop.constant.UserConstant;
import com.baidu.shop.dto.NoteDTO;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.RedisRepository;
import com.baidu.shop.business.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.LuosimaoDuanxinUtil;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/13
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisRepository redisRepository;

    @Override
    public UserEntity getUser(Integer id) {
        UserEntity userEntity = userMapper.selectByPrimaryKey(id);
        return userEntity;
    }

    @Override
    public Result<JsonObject> checkValidCode(NoteDTO noteDTO) {
        String s = redisRepository.get(UserConstant.USER_PHONE_CODE_PRE + noteDTO.getPhone());
        if (!noteDTO.getValidCode().equals(s)) {
            return this.setResultError(HTTPStatus.VALID_CODE_ERROR ,"验证码不正确");
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> sendValidCode(UserDTO userDTO) {
        //随机六位数验证码
        String code = (int)((Math.random() * 9 + 1) * 100000 ) +"" ;
        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);

        redisRepository.set( UserConstant.USER_PHONE_CODE_PRE + userDTO.getPhone(), code);
        redisRepository.expire(UserConstant.USER_MESSAGE_PRE + userDTO.getPhone(),120);

        //发送短信验证码
        //LuosimaoDuanxinUtil.SendCode(userDTO.getPhone(),code);
        //发送语音验证码
        String aaa = "世界你好,恁辛苦了";
        //LuosimaoDuanxinUtil.sendSpeak(userDTO.getPhone(),code);
        return this.setResultSuccess();

    }

    @Override
    public Result<List<UserEntity>> checkUserNameOrPhone(String value, Integer type) {
        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (value != null && type != null){
            if (type == RegisterConstant.USER_USERNAME_TYPE){
                criteria.andEqualTo("username",value);
            }else if(type == RegisterConstant.USER_PHONENAME_TYPE){
                criteria.andEqualTo("phone",value);
            }
        }
        List<UserEntity> entityList = userMapper.selectByExample(example);
        return this.setResultSuccess(entityList);
    }

    @Override
    public Result<JsonObject> register(UserDTO userDTO) {

        if(userDTO != null){
            UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
            userEntity.setCreated(new Date());
            if (userEntity.getPassword() != null){
                userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));
                userMapper.insertSelective(userEntity);
            }
        }
        return this.setResultSuccess();
    }
}
