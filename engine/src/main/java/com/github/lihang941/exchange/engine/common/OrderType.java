package com.github.lihang941.exchange.engine.common;

import com.github.lihang941.common.exception.ErrorMsgException;

/**
 * 订单类型
 */
public interface OrderType {
    /**
     * 限价买
     */
    Integer buy_limit = 0;

    /**
     * 限价卖
     */
    Integer sell_limit = 1;

    static boolean buy(Integer type) {
        if (type == OrderType.buy_limit) {
            return true;
        } else if (type == OrderType.sell_limit) {
            return false;
        } else {
            throw new ErrorMsgException("非法的类型");
        }
    }

}
