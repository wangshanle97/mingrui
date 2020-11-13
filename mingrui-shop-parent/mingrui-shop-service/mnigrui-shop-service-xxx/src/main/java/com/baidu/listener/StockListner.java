package com.baidu.listener;

import com.baidu.shop.business.GoodsService;
import com.baidu.shop.constant.MrMessageConstant;
import com.baidu.shop.dto.StockDTO;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName GoodsListner
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/12
 * @Version V1.0
 **/
@Component
@Slf4j
public class StockListner {

   @Autowired
   private GoodsService goodsService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            value = MrMessageConstant.STOCK_QUEUE_UPDATE,
                            durable = "true"
                    ),
                    exchange = @Exchange(
                            value = MrMessageConstant.EXCHANGE,
                            ignoreDeclarationExceptions = "true",
                            type = ExchangeTypes.TOPIC
                    ),
                    key =  MrMessageConstant.STOCK_ROUT_KEY_UPDATE
            )
    )
    @RabbitHandler
    public void update(@Payload StockDTO stockDTO, Channel channel , @Headers Map<String,String> headers) throws IOException {

        log.info("服务接受到需要修改数据的消息: " + stockDTO.getSkuId() + stockDTO.getStock());

        goodsService.updateStock(stockDTO);
        Long s = Long.valueOf(headers.get(AmqpHeaders.DELIVERY_TAG));

//        shopElasticsearchService.saveData(Integer.parseInt(new String(message.getBody())));
        channel.basicAck(s, true);

    }


}
