package com.baidu.shop.component;

import com.baidu.shop.constant.MrMessageConstant;
import com.baidu.shop.dto.StockDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @ClassName MrRabbitmq
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/23
 * @Version V1.0
 **/
@Slf4j
@Component
public class MrRabbitmq implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    private RabbitTemplate rabbitTemplate;


    @Autowired
    public MrRabbitmq(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;

        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(this);
    }

    public void send(StockDTO stockDTO, String routingKey){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(MrMessageConstant.EXCHANGE,routingKey,stockDTO,correlationData);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {

        if (b){
            log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,b,s);
        }else{
            log.info("消息发送失败:correlationData({}),ack({}),cause({})",correlationData,b,s);
        }
    }

    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        log.warn("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",s1,s2,i,s,message);
    }
}
