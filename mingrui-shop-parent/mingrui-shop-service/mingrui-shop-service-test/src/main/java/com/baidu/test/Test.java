package com.baidu.test;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName Test
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/16
 * @Version V1.0
 **/
@RestController
public class Test {

    @Resource
    private Redisson redisson;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Integer i = 50;

    @GetMapping("/test")
    public String test() throws Exception{

        //String uuid = UUID.randomUUID() + "";

        String lockKey = "lockKey";

        RLock redissonLock = redisson.getLock(lockKey);

        try{
//            stringRedisTemplate.expire(lockKey,10,TimeUnit.SECONDS)
//            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uuid,10, TimeUnit.SECONDS);
//            if (!result){
//                return "error";
//            }

            //这一行代码相当于setIfAbsent(lockKey, uuid,30, TimeUnit.SECONDS);  默认30秒
            redissonLock.lock();//默认定时续命

            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if (stock > 0){
                int realStock = i -1;
                stringRedisTemplate.opsForValue().set("stock",realStock +"");
                System.out.println("扣件成功,剩余:" + realStock);
            } else{
                System.out.println("扣减失败,没有了");
            }
        } finally {
            redissonLock.unlock();
//            if (uuid.equals(stringRedisTemplate.opsForValue().get(lockKey))){
//                stringRedisTemplate.delete(lockKey);
//            }
        }


        return "success";
    }

}
