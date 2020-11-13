package com.baidu.shop.busioness.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OrderService;
import com.baidu.shop.component.MrRabbitmq;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.MrMessageConstant;
import com.baidu.shop.constant.MrShopConstant;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.OrderDetailEntity;
import com.baidu.shop.entity.OrderEntity;
import com.baidu.shop.entity.OrderStatusEntity;
import com.baidu.shop.feign.AddressFeign;
import com.baidu.shop.mapper.OrderDetailMapper;
import com.baidu.shop.mapper.OrderMapper;
import com.baidu.shop.mapper.OrderStatusMapper;
import com.baidu.shop.redis.RedisReponsitory;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.*;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/21
 * @Version V1.0
 **/
@RestController
public class OrderServiceImpl extends BaseApiService implements OrderService {

    @Resource
    private MrRabbitmq mrRabbitmq;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private OrderStatusMapper orderStatusMapper;

    @Resource
    private OrderMapper orderMapper;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private IdWorker idWorker;

    @Resource
    private RedisReponsitory redisReponsitory;

    @Resource
    private AddressFeign addressFeign;



    @Override
    public Result<JSONObject> getPayStatus(String token) {
        UserInfo userInfo = null;
        try {
            userInfo = JwtUtil.getInfoFromToken(token, jwtConfig.getPublicKey());
            Example example = new Example(OrderEntity.class);
            example.createCriteria().andEqualTo("userId",userInfo.getId());
            List<OrderEntity> orderEntities = orderMapper.selectByExample(example);

            List<OrderStatusEntity> list = new ArrayList<>();
            List<OrderDetailEntity> detailList = new ArrayList<>();
            OrderInfo orderInfo = null;
            for(OrderEntity order : orderEntities){
                orderInfo = BaiduBeanUtil.copyProperties(orderEntities.get(0), OrderInfo.class);
                OrderStatusEntity statusEntity = orderStatusMapper.selectByPrimaryKey(order.getOrderId());
                if (statusEntity.getStatus() == 1){
                    list.add(statusEntity);
                    List<OrderDetailEntity> detail = this.getDetail(order.getOrderId());
                    detailList.add(detail.get(0));
                }
            }
            orderInfo.setOrderStatusEntityList(list);
            orderInfo.setOrderDetailEntityList(detailList);
            return this.setResultSuccess(orderInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess("系统异常");
    }

    private List<OrderDetailEntity> getDetail(String orderId){
        Example example = new Example(OrderDetailEntity.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        List<OrderDetailEntity> orderDetailEntities = orderDetailMapper.selectByExample(example);
        return orderDetailEntities;
    }

   /* private List<OrderStatusEntity> getStatus(String orderId){
        Example example = new Example(OrderStatusEntity.class);
        example.createCriteria().andEqualTo("status",1);
        example.createCriteria().andEqualTo("orderId",orderId);

        List<OrderStatusEntity> orderStatusEntityList = orderStatusMapper.selectByExample(example);
        return orderStatusEntityList;
    }*/


    @Override
    public Result<JsonObject> delOrder(String id) {
        if(ObjectUtil.isNotNull(id)){
            Example example = new Example(OrderEntity.class);
            example.createCriteria().andEqualTo("orderId",id);
            orderMapper.deleteByExample(example);
            orderStatusMapper.deleteByExample(example);
            orderDetailMapper.deleteByExample(example);
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<List<OrderInfo>> getUserMessage( String token) {
        UserInfo userInfo = null;
        try {
            userInfo = JwtUtil.getInfoFromToken(token, jwtConfig.getPublicKey());
            Example example = new Example(OrderEntity.class);
            example.createCriteria().andEqualTo("userId",userInfo.getId());
            List<OrderEntity> orderEntities = orderMapper.selectByExample(example);
            List<OrderInfo> list = orderEntities.stream().map(order -> {
                Result<OrderInfo> info = this.getOrderInfoByOrderId(order.getOrderId());
                if (info.getCode() == 200) {
                    OrderInfo data = info.getData();
                    return data;
                }
                return null;
            }).collect(Collectors.toList());
            return this.setResultSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return this.setResultError("失败");
    }

    public Result<OrderInfo> getOrderByOrderId(String orderId) {
        Example example1 = new Example(OrderStatusEntity.class);
        example1.createCriteria().andEqualTo("status",1).andEqualTo("orderId",orderId);

        List<OrderStatusEntity> orderStatusList = orderStatusMapper.selectByExample(example1);

        if (orderStatusList.size() > 0){
            OrderEntity orderEntity = orderMapper.selectByPrimaryKey(orderStatusList.get(0).getOrderId());
            OrderInfo orderInfo = BaiduBeanUtil.copyProperties(orderEntity, OrderInfo.class);

            Example example = new Example(OrderDetailEntity.class);
            example.createCriteria().andEqualTo("orderId",orderEntity.getOrderId());
            List<OrderDetailEntity> orderDetailList = orderDetailMapper.selectByExample(example);
            orderInfo.setOrderStatusEntity(orderStatusList.get(0));
            orderInfo.setOrderDetailEntityList(orderDetailList);

            return this.setResultSuccess(orderInfo);
        }
        return null;
    }

    @Override
    public Result<OrderInfo> getOrderInfoByOrderId(String orderId) {
        OrderEntity orderEntity = orderMapper.selectByPrimaryKey(orderId);
        OrderInfo orderInfo = BaiduBeanUtil.copyProperties(orderEntity, OrderInfo.class);

        Example example = new Example(OrderDetailEntity.class);
        example.createCriteria().andEqualTo("orderId",orderEntity.getOrderId());

        List<OrderDetailEntity> orderDetailList = orderDetailMapper.selectByExample(example);
        orderInfo.setOrderDetailEntityList(orderDetailList);

        OrderStatusEntity orderStatusEntity = orderStatusMapper.selectByPrimaryKey(orderInfo.getOrderId());

        orderInfo.setOrderStatusEntity(orderStatusEntity);


        return this.setResultSuccess(orderInfo);
    }

    @Override
    @Transactional
    public Result<String> createOrder(OrderDTO orderDTO, String token) {
        long id = idWorker.nextId();
        String orderId = String.valueOf(id);
        Result<List<AddressDTO>> address = addressFeign.getAddress(Integer.valueOf(orderDTO.getAddrId()));
        AddressDTO addressById = address.getData().get(0);
        try {
            Date date = new Date();
            UserInfo userInfo = JwtUtil.getInfoFromToken(token, jwtConfig.getPublicKey());
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setOrderId(orderId);
            orderEntity.setUserId(userInfo.getId()+"");
            orderEntity.setSourceType(1);
            orderEntity.setBuyerRate(1);
            orderEntity.setInvoiceType(1);
            orderEntity.setPaymentType(orderDTO.getPayType());
            orderEntity.setCreateTime(date);
            orderEntity.setBuyerNick("不容易啊");
            orderEntity.setReceiver(addressById.getName());
            orderEntity.setBuyerNick(userInfo.getUsername());
            orderEntity.setReceiverAddress(addressById.getDetailed());
            orderEntity.setReceiverMobile(addressById.getPhone());
            orderEntity.setReceiverZip(addressById.getZipCode());


            List<Long> longs = Arrays.asList(0L);
            StockDTO stockDTO = new StockDTO();

            List<OrderDetailEntity> list = Arrays.asList(orderDTO.getSkuIds().split(",")).stream().map(skuId -> {
                Car car = redisReponsitory.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(), skuId, Car.class);
                if (car == null) {
                    throw new RuntimeException("异常");
                }
                stockDTO.setSkuId(car.getSkuId());
                stockDTO.setStock(car.getNum());
                OrderDetailEntity detailEntity = new OrderDetailEntity();
                detailEntity.setSkuId(Long.valueOf(skuId));
                detailEntity.setTitle(car.getTitle());
                detailEntity.setPrice(car.getPrice());
                detailEntity.setOrderId(orderId);
                detailEntity.setNum(car.getNum());
                detailEntity.setImage(car.getImage());
                detailEntity.setOwnSpec(car.getOwnSpec());
                longs.set(0, car.getPrice() * car.getNum() + longs.get(0));
                return detailEntity;
            }).collect(Collectors.toList());

            orderEntity.setActualPay(longs.get(0));
            orderEntity.setTotalPay(longs.get(0));
            OrderStatusEntity orderStatusEntity = this.setOrderStatus(orderId, date);

            orderDetailMapper.insertList(list);
            orderMapper.insertSelective(orderEntity);
            orderStatusMapper.insertSelective(orderStatusEntity);

            Arrays.asList(orderDTO.getSkuIds().split(",")).stream().forEach(skuIds ->{
                redisReponsitory.delHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(),skuIds);

            });
            mrRabbitmq.send(stockDTO, MrMessageConstant.STOCK_ROUT_KEY_UPDATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResult(HTTPStatus.OK,"",orderId +"");
    }
    private OrderStatusEntity setOrderStatus(String  orderId ,Date date){
        OrderStatusEntity orderStatusEntity = new OrderStatusEntity();
        orderStatusEntity.setOrderId(orderId);
        orderStatusEntity.setCreateTime(date);
        orderStatusEntity.setStatus(1);
        return orderStatusEntity;
    }
}
