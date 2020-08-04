package com.github.lihang941.exchange.engine.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * 订单对象
 */
@Accessors(chain = true)
@Data
public class OrderData implements Comparable<OrderData> {

    /**
     * 订单ID (必须要顺序创建的订单)
     */
    private Long id;

    /**
     * 交易对ID
     */
    private Long symbolId;

    /**
     * 数量
     */
    private BigDecimal amount;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 类型
     * buy-limit：限价买, sell-limit：限价卖
     */
    private Integer type;

    /**
     * 已成交数量
     */
    private BigDecimal fieldAmount;

    /**
     * 已成交总金额
     */
    private BigDecimal fieldCashAmount;


    /**
     * submitted 已提交, partial-filled 部分成交, partial-canceled 部分成交撤销, filled 完全成交, canceled 已撤销， created
     */
    private Integer state;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderData orderData = (OrderData) o;
        return Objects.equals(id, orderData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(OrderData o) {
        return this.getId().compareTo(o.getId());
    }
}
