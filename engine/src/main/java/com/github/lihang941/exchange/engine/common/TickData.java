package com.github.lihang941.exchange.engine.common;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 买一卖一逐笔行情
 */
public class TickData {

    @ApiModelProperty("盘口更新时间")
    private Long quoteTime;
    @ApiModelProperty("买一价")
    private BigDecimal bid;
    @ApiModelProperty("买一量")
    private BigDecimal bidSize;
    @ApiModelProperty("卖一价")
    private BigDecimal ask;
    @ApiModelProperty("卖一量")
    private BigDecimal askSize;

    public Long getQuoteTime() {
        return quoteTime;
    }

    public TickData setQuoteTime(Long quoteTime) {
        this.quoteTime = quoteTime;
        return this;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public TickData setBid(BigDecimal bid) {
        this.bid = bid;
        return this;
    }

    public BigDecimal getBidSize() {
        return bidSize;
    }

    public TickData setBidSize(BigDecimal bidSize) {
        this.bidSize = bidSize;
        return this;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public TickData setAsk(BigDecimal ask) {
        this.ask = ask;
        return this;
    }

    public BigDecimal getAskSize() {
        return askSize;
    }

    public TickData setAskSize(BigDecimal askSize) {
        this.askSize = askSize;
        return this;
    }
}
