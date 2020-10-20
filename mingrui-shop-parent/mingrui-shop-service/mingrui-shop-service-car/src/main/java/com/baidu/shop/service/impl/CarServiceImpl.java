package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.MrShopConstant;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.SkuEntity;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.redis.RedisReponsitory;
import com.baidu.shop.service.CarService;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.JwtUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CarServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/19
 * @Version V1.0
 **/
@RestController
@Slf4j
public class CarServiceImpl extends BaseApiService implements CarService {


    @Autowired
    private RedisReponsitory redisReponsitory;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<List<Car>> carNumUpdate(Long skuId, Integer type, String token) {
        try {
            UserInfo userInfo = JwtUtil.getInfoFromToken(token, jwtConfig.getPublicKey());
            Car redisCar = redisReponsitory.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(),
                    skuId + "", Car.class);

            redisCar.setNum(type == MrShopConstant.CAR_OPRETION_INCREMENT ? redisCar.getNum() +1 : redisCar.getNum() -1);

            redisReponsitory.setHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(),
                    skuId + "", JSONUtil.toJsonString(redisCar));
        } catch (Exception e) {
            e.printStackTrace();
        }


        return this.setResultSuccess();
    }

    @Override
    public Result<List<Car>> getCurrentUserGoodsCar(String token) {
        List<Car> carList = new ArrayList<>();
        try {
            UserInfo userInfo = JwtUtil.getInfoFromToken(token, jwtConfig.getPublicKey());

            Map<String, String> map = redisReponsitory.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId());
            map.forEach((key,value) -> carList.add(JSONUtil.toBean(value,Car.class)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess(carList);
    }

    @Override
    public Result<JSONObject> mergeCar(String clintCarList, String token) {


        JSONObject jsonObject = JSONObject.parseObject(clintCarList);
        List<Car> carList = JSONObject.parseArray(jsonObject.get("clintCarList").toString(), Car.class);

        carList.stream().forEach(car ->{
            this.addCar(car,token);
        });
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> addCar(Car car,String token) {
        System.out.println(car+"---"+token);

        try {
            UserInfo userInfo = JwtUtil.getInfoFromToken(token, jwtConfig.getPublicKey());
            Car redisCar = redisReponsitory.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(),
                    car.getSkuId() + "", Car.class);

            Car saveCar = null;
            log.debug("通过key:{},skuid:{} 获取到的数据为 :{}",MrShopConstant.USER_GOODS_CAR_PRE+userInfo.getId()
                    ,car.getSkuId(),redisCar);
            if (ObjectUtil.isNotNull(redisCar)){
                redisCar.setNum(car.getNum() + redisCar.getNum());
                saveCar = redisCar;
                log.debug("当前用户购物车中有将要新增的商品 , 重新设置num:{}" + redisCar.getNum());
            }else{
                Result<SkuEntity> skuBySkuId = goodsFeign.getSkuBySkuId(car.getSkuId());
                if (skuBySkuId.getCode() == 200){
                    SkuEntity data = skuBySkuId.getData();
                    car.setTitle(data.getTitle());
                    car.setImage(StringUtil.isNotEmpty(data.getImages()) ? data.getImages().split(",")[0] : "");

                    Map<String, Object> map = JSONUtil.toMap(data.getOwnSpec());//key:id
                    //value: 规格参数值
                    //遍历map
                    //feign调用通过paramId查询info的接口
                    //重新组装map
                    //将map转为json字符串

                    car.setOwnSpec(data.getOwnSpec());
                    car.setPrice(Long.valueOf(data.getPrice()));
                    car.setUserId(userInfo.getId());
                    saveCar = car;

                    log.debug("新增商品至购物车redis,KEY :{},skuid:{},car:{}",MrShopConstant.USER_GOODS_CAR_PRE+userInfo.getId(),car.getSkuId(),JSONUtil.toJsonString(car));
                }
            }
            redisReponsitory.setHash(MrShopConstant.USER_GOODS_CAR_PRE +userInfo.getId(),car.getSkuId() + "", JSONUtil.toJsonString(saveCar));
            log.debug("新增redis成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess();
    }
}
