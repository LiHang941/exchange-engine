package com.github.lihang941.exchange.engine.service;


import com.github.lihang941.exchange.engine.common.OrderData;
import com.github.lihang941.exchange.engine.common.OrderMatchResult;

public interface OrderEvent {

    /**
     * 撮合成功回调
     *
     * @param orderMatchResult
     */
    default void match(OrderMatchResult orderMatchResult) {
        // TODO 处理撮合结算
        // TODO 处理K线
    }

    /**
     * 监听订单添加到订单簿回调
     *
     * @param orderData
     */
    default void addOrder(OrderData orderData) {
        // TODO 处理你的订单
    }



    /**
     * 监听订单撤销回调
     *
     * @param orderData
     */
    default void cancelOrder(OrderData orderData) {
        // TODO 处理订单
    }

}
