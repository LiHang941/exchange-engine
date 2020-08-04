package com.github.lihang941.exchange.engine;

import com.alibaba.fastjson.JSON;
import com.github.lihang941.common.utils.DateUtil2;
import com.github.lihang941.common.utils.RandomCode;
import com.github.lihang941.exchange.engine.common.OrderData;
import com.github.lihang941.exchange.engine.common.OrderState;
import com.github.lihang941.exchange.engine.common.OrderType;
import com.github.lihang941.exchange.engine.service.MatchOrderDataBase;
import com.github.lihang941.exchange.engine.service.producer.OrderProducer;
import com.github.lihang941.exchange.engine.tool.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@SpringBootApplication
public class AppServiceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AppServiceApplication.class, args);
    }

    @Autowired
    private MatchOrderDataBase matchOrderDataBase;

    @Autowired
    private OrderProducer orderProducer;

    //撮合测试
    @Override
    public void run(String... args) throws Exception {
        log.info("开始时间 {}", DateUtil2.getDateTimeFormat(new Date()));
        SnowFlake snowFlake = new SnowFlake(2, 3);
        List<BigDecimal> price = new ArrayList<BigDecimal>() {
            {
                add(new BigDecimal("1"));
                add(new BigDecimal("2"));
                add(new BigDecimal("3"));
                add(new BigDecimal("4"));
                add(new BigDecimal("5"));
            }
        };

        Long symbolId = 1L;

        for (int i = 0; i < 1000000; i++) {
            OrderData orderData = new OrderData();
            orderData.setId(snowFlake.nextId());
            orderData.setSymbolId(1L);
            orderData.setAmount(new BigDecimal("10000"));
            orderData.setPrice(price.get(RandomCode.getRandom().nextInt(price.size())));
            orderData.setType(RandomCode.getRandom().nextBoolean() ? OrderType.sell_limit : OrderType.buy_limit);
            orderData.setFieldAmount(new BigDecimal("0"));
            orderData.setFieldCashAmount(new BigDecimal("0"));
            orderData.setState(OrderState.create);
            orderProducer.doMatch(orderData);
        }

        orderProducer.submit(() -> {
            // TODO 获取盘口数据 一般通过定时任务定时刷新盘口数据 10MS的刷新
            log.info("买一卖一 {}", JSON.toJSONString(matchOrderDataBase.getTickData(symbolId)));
            log.info("深度数据 {}", JSON.toJSONString(matchOrderDataBase.getDepthData(symbolId, 8)));
        });
    }
}
