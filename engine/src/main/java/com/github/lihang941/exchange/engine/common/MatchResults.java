package com.github.lihang941.exchange.engine.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
public class MatchResults {

    /**
     * 匹配ID
     */
    private Long matchId;
    /**
     * 交易对ID
     */
    private Long symbolId;

    /**
     * 成交价格
     */
    private BigDecimal price;
    /**
     * 成交数量
     */
    private BigDecimal filledAmount;

    /**
     * 成交时间
     */
    private Date createdAt;

}
