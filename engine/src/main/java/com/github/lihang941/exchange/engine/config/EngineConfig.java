/**
 * @filename: DisruptorConfig.java 2019年12月19日
 * @project match-engine  V1.0
 * Copyright(c) 2020 flying-cattle Co. Ltd.
 * All right reserved.
 */
package com.github.lihang941.exchange.engine.config;


import com.github.lihang941.exchange.engine.common.OrderData;
import com.github.lihang941.exchange.engine.common.OrderMatchResult;
import com.github.lihang941.exchange.engine.service.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@ComponentScan("com.github.lihang941.exchange.engine")
@Slf4j
@Configuration
public class EngineConfig {


    @ConditionalOnMissingBean
    @Bean
    public OrderEvent orderEvent() {
        return new OrderEvent() {
            @Override
            public void match(OrderMatchResult orderMatchResult) {
                // log.info("订单匹配:{}", JSON.toJSONString(orderMatchResult));
            }

            @Override
            public void addOrder(OrderData orderData) {
                // log.info("添加订单:{}", JSON.toJSONString(orderData));
            }

            @Override
            public void cancelOrder(OrderData orderData) {
                // log.info("取消订单:{}", JSON.toJSONString(orderData));
            }
        };
    }
}
