package com.github.lihang941.exchange.engine.common;

/**
 * 订单状态
 */
public interface OrderState {
    /**
     * 未提交
     */
    Integer create = 0;

    /**
     * 已提交
     */
    Integer submitted = 1;
    /**
     * 部分成交
     */
    Integer partial_filled = 2;
    /**
     * 部分成交撤销
     */
    Integer partial_canceled = 3;
    /**
     * 完全成交
     */
    Integer filled = 4;

    /**
     * 已撤销
     */
    Integer canceled = 5;


}
