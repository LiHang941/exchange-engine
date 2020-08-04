package com.github.lihang941.exchange.engine.tool;


import com.github.lihang941.exchange.engine.common.OrderData;

public class OrderTool {


    /**
     * 获取对应限价交易对的买/卖盘KEY
     *
     * @return
     */
    public static String getOrderPriceListKey(Long symbolId, boolean buy) {
        return symbolId + (buy ? "-MATCH-BID" : "-MATCH-ASK");
    }


    /**
     * 复制订单
     *
     * @return
     */
    public static OrderData copyOrderData(OrderData orderData) {
        OrderData save = new OrderData();
        save.setId(orderData.getId());
        save.setSymbolId(orderData.getSymbolId());
        save.setAmount(orderData.getAmount());
        save.setPrice(orderData.getPrice());
        save.setType(orderData.getType());
        save.setFieldAmount(orderData.getFieldAmount());
        save.setFieldCashAmount(orderData.getFieldCashAmount());
        save.setState(orderData.getState());
        return save;
    }


}
