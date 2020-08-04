package com.github.lihang941.exchange.engine.service.producer;

import com.github.lihang941.exchange.engine.common.OrderData;
import com.github.lihang941.exchange.engine.tool.OrderTool;
import com.github.lihang941.exchange.engine.service.handler.MatchOrderWorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 订单生产者
 */
@Slf4j
@Component
public class OrderProducer implements DisposableBean {

    @Autowired
    private MatchOrderWorkHandler matchOrderWorkHandler;

    private volatile Boolean producer = true;

    private ExecutorService executorService = Executors.newFixedThreadPool(1, r -> new Thread(r, "ORDER-PRODUCER"));

    /**
     * 提交订单
     *
     * @param input
     */
    public void doMatch(OrderData input) {
        submit(() -> {
            matchOrderWorkHandler.doMatch(OrderTool.copyOrderData(input));
        });
    }

    /**
     * 取消订单
     *
     * @param consumer 取消任务回调 <订单ID,处理状态>
     * @param inputs   订单
     */
    public void cancelOrder(Consumer<Map<Long, Boolean>> consumer, OrderData... inputs) {
        submit(() -> {
            HashMap<Long, Boolean> map = new HashMap<>();
            Stream.of(inputs).forEach(input -> {
                boolean b = matchOrderWorkHandler.cancelOrder(OrderTool.copyOrderData(input));
                map.put(input.getId(), b);
            });
            if (consumer != null) {
                try {
                    consumer.accept(map);
                } catch (Exception e) {
                    log.warn("处理订阅失败 忽略错误", e);
                }
            }
        });
    }

    /**
     * 提交需要线性执行的任务
     *
     * 如果执行出现错误，那么整个撮合将会停止
     *
     * @param runnable
     */
    public void submit(Runnable runnable) {
        try {
            executorService.submit(() -> {
                try {
                    if (producer) {
                        Long startTime = System.currentTimeMillis();
                        runnable.run();
                        log.info("任务消费 {} ms", System.currentTimeMillis() - startTime);
                    } else {
                        log.error("消费者已关闭 任务没有进入队列");
                    }
                } catch (Exception ex) {
                    log.error("process data error ex ==[{}]", ex.getMessage());
                    stopProducer();
                }
            });
        } catch (Exception e) {
            log.error("执行任务失败 error:{}", e.getMessage());
        }
    }


    public void stopProducer() {
        this.producer = false;
    }

    public Boolean getStopStatus() {
        return producer == false;
    }

    @Override
    public void destroy() throws Exception {
        try {
            stopProducer();
            executorService.shutdown();
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("停止线程错误", e);
        }
    }
}
