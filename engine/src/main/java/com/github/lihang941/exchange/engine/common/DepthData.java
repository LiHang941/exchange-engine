package com.github.lihang941.exchange.engine.common;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ApiModel("深度数据")
public class DepthData {
    @ApiModelProperty("当前的所有买单 [price, quote volume]")
    private List<List<BigDecimal>> bids = new ArrayList<List<BigDecimal>>();
    @ApiModelProperty("当前的所有卖单 [price, quote volume]")
    private List<List<BigDecimal>> asks = new ArrayList<List<BigDecimal>>();

    @ApiModelProperty("最新成交价")
    private BigDecimal lastExchangePrice;
    @ApiModelProperty("价格精度")
    private Integer pricePrecision;

    @ApiModelProperty("时间戳 毫秒")
    private Long ts = System.currentTimeMillis();

    public List<List<BigDecimal>> getBids() {
        return bids;
    }

    public DepthData setBids(List<List<BigDecimal>> bids) {
        this.bids = bids;
        return this;
    }

    public List<List<BigDecimal>> getAsks() {
        return asks;
    }

    public DepthData setAsks(List<List<BigDecimal>> asks) {
        this.asks = asks;
        return this;
    }

    public Long getTs() {
        return ts;
    }

    public DepthData setTs(Long ts) {
        this.ts = ts;
        return this;
    }

    public BigDecimal getLastExchangePrice() {
        return lastExchangePrice;
    }

    public DepthData setLastExchangePrice(BigDecimal lastExchangePrice) {
        this.lastExchangePrice = lastExchangePrice;
        return this;
    }

    public Integer getPricePrecision() {
        return pricePrecision;
    }

    public DepthData setPricePrecision(Integer pricePrecision) {
        this.pricePrecision = pricePrecision;
        return this;
    }
}
