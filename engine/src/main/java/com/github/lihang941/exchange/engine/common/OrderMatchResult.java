package com.github.lihang941.exchange.engine.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 订单匹配结果
 */
@Accessors(chain = true)
@Data
public class OrderMatchResult {
    /**
     * 发起订单
     */
    private OrderData orderData;
    /**
     * 匹配到的订单
     */
    private List<OrderData> matchOrderDataList;
    /**
     * 撮合成交记录
     */
    private List<MatchResults> matchResultsList;
}
