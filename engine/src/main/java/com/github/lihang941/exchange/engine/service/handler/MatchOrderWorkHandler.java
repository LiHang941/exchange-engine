package com.github.lihang941.exchange.engine.service.handler;

import com.alibaba.fastjson.JSON;
import com.github.lihang941.common.exception.ErrorMsgException;
import com.github.lihang941.exchange.engine.common.OrderData;
import com.github.lihang941.exchange.engine.common.OrderState;
import com.github.lihang941.exchange.engine.common.OrderType;
import com.github.lihang941.exchange.engine.service.MatchOrderDataBase;
import com.github.lihang941.exchange.engine.service.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * 订单业务处理
 */
@Slf4j
@Component
public class MatchOrderWorkHandler {

    @Autowired
    private MatchOrderDataBase matchOrderDataBase;

    @Autowired
    private OrderEvent orderEvent;


    private HashSet<Integer> inputType = new HashSet<Integer>() {
        {
            add(OrderState.create);
            add(OrderState.partial_filled);
            add(OrderState.submitted);
        }
    };

    private HashSet<Integer> outType = new HashSet<Integer>() {
        {
            add(OrderState.partial_canceled);
            add(OrderState.canceled);
        }
    };


    private HashSet<Integer> handleOrderTpe = new HashSet<Integer>() {
        {
            add(OrderType.buy_limit);
            add(OrderType.sell_limit);
        }
    };

    public void doMatch(OrderData orderData) {
        if (inputType.contains(orderData.getState()) && handleOrderTpe.contains(orderData.getType())) {
            matchOrderDataBase.doMatch(orderData);
        } else {
            log.warn("非法的订单 {}", JSON.toJSONString(orderData));
            throw new ErrorMsgException("非法的订单");
        }
    }

    public boolean cancelOrder(OrderData event) {
        if (outType.contains(event.getState())) {
            OrderData temp = matchOrderDataBase.cancelOrder(event);
            if (temp != null) {
                orderEvent.cancelOrder(temp);
                return true;
            }
        } else {
            log.warn("非法的取消订单 {}", JSON.toJSONString(event));
            throw new ErrorMsgException("非法的取消订单");
        }
        return false;
    }


}
