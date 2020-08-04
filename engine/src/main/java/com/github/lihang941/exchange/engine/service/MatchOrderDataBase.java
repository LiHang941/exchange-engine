package com.github.lihang941.exchange.engine.service;

import com.alibaba.fastjson.JSON;
import com.github.lihang941.common.exception.ErrorMsgException;
import com.github.lihang941.exchange.engine.common.*;
import com.github.lihang941.exchange.engine.tool.OrderTool;
import com.github.lihang941.exchange.engine.tool.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 订单处理
 */
@Slf4j
@Service
public class MatchOrderDataBase {


    public static final SnowFlake exchangeSnowFlake = new SnowFlake(2, 3);

    @Autowired
    private OrderEvent orderEvent;

    /**
     * 订单薄
     */
    private HashMap<String, TreeMap<BigDecimal, TreeSet<OrderData>>> symbolOrderPriceListHashMap = new HashMap<>();


    /**
     * 撮合订单
     *
     * @param orderData
     */
    public void doMatch(OrderData orderData) {

        int updateFlag = 0;

        OrderMatchResult orderMatchResult = new OrderMatchResult();
        orderMatchResult.setMatchOrderDataList(new ArrayList<>());
        orderMatchResult.setMatchResultsList(new ArrayList<>());

        if (orderData.getState() == OrderState.create) {
            orderData.setState(OrderState.submitted);
        }

        boolean buy = OrderType.buy(orderData.getType());
        // 获取对手盘
        TreeMap<BigDecimal, TreeSet<OrderData>> orderPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(orderData.getSymbolId(), !buy));
        if (orderPriceQueueTreeMap != null) {
            NavigableSet<BigDecimal> priceSortList = buy
                    ? orderPriceQueueTreeMap.navigableKeySet().headSet(orderData.getPrice(), true)   // 卖队列
                    : orderPriceQueueTreeMap.navigableKeySet().tailSet(orderData.getPrice(), true).descendingSet();  // 买队列


            Iterator<BigDecimal> iterator = priceSortList.iterator();


            while (iterator.hasNext()) {
                BigDecimal toPrice = iterator.next();
                TreeSet<OrderData> toOrderDataTreeSet = orderPriceQueueTreeMap.get(toPrice);
                if (toOrderDataTreeSet == null || toOrderDataTreeSet.size() == 0) {
                    continue;
                }

                OrderData matchOrder = toOrderDataTreeSet.first();
                while (matchOrder != null) {


                    updateFlag++;

                    BigDecimal remainderAmount = orderData.getAmount().subtract(orderData.getFieldAmount());
                    BigDecimal toRemainderAmount = matchOrder.getAmount().subtract(matchOrder.getFieldAmount());

                    BigDecimal amount;
                    // 能把匹配订单吃完
                    if (remainderAmount.compareTo(toRemainderAmount) >= 0) {
                        amount = toRemainderAmount;
                    } else {
                        amount = remainderAmount;
                    }
                    BigDecimal cashAmount = matchOrder.getPrice().multiply(amount);

                    orderData.setFieldAmount(orderData.getFieldAmount().add(amount));
                    orderData.setFieldCashAmount(orderData.getFieldCashAmount().add(cashAmount));

                    matchOrder.setFieldAmount(matchOrder.getFieldAmount().add(amount));
                    matchOrder.setFieldCashAmount(matchOrder.getFieldCashAmount().add(cashAmount));


                    handleOrderState(orderData);
                    handleOrderState(matchOrder);


                    orderMatchResult.getMatchOrderDataList().add(OrderTool.copyOrderData(matchOrder));
                    MatchResults matchResults = new MatchResults();
                    matchResults.setMatchId(exchangeSnowFlake.nextId());
                    matchResults.setSymbolId(orderData.getSymbolId());
                    matchResults.setPrice(matchOrder.getPrice());
                    matchResults.setFilledAmount(amount);
                    matchResults.setCreatedAt(new Date());
                    orderMatchResult.getMatchResultsList().add(matchResults);
                    orderMatchResult.setOrderData(OrderTool.copyOrderData(orderData));
                    if (matchOrder.getState() == OrderState.filled) {
                        boolean b = removeOrder(matchOrder, toOrderDataTreeSet, iterator);
                        if (!b) {
                            log.error("移除订单失败 input:{} toOrder:{}", JSON.toJSONString(orderData), JSON.toJSONString(matchOrder));
                            throw new ErrorMsgException("移除订单失败");
                        }
                    }

                    if (orderData.getState() == OrderState.filled) {
                        // 结束
                        orderEvent.match(orderMatchResult);//匹配完成
                        return;
                    }

                    if (toOrderDataTreeSet.size() > 0) {
                        matchOrder = toOrderDataTreeSet.first();
                    } else {
                        matchOrder = null;
                    }
                }
            }
        }
        //
        if (updateFlag > 0) {
            orderEvent.match(orderMatchResult);
        }

        addOrder(orderData);
    }


    /**
     * 获取深度
     *
     * @param symbolId 交易对ID
     * @param precision 精度
     * @return
     */
    public Map<String, DepthData> getDepthData(Long symbolId, int precision) {


        Map<String, DepthData> depthDataMap = new HashMap<>();

        TreeMap<BigDecimal, TreeSet<OrderData>> orderBuyPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(symbolId, true));
        TreeMap<BigDecimal, TreeSet<OrderData>> orderSellPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(symbolId, false));

        for (int i = 0; i < 6; i++) {
            depthDataMap.put(String.valueOf(i), new DepthData());
        }


        if (orderBuyPriceQueueTreeMap != null && orderBuyPriceQueueTreeMap.size() > 0) {
            Set<Map.Entry<BigDecimal, TreeSet<OrderData>>> buyEntries = orderBuyPriceQueueTreeMap.descendingMap().entrySet();
            for (int i = 0; i < 6; i++) {

                Iterator<Map.Entry<BigDecimal, TreeSet<OrderData>>> iterator = buyEntries.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<BigDecimal, TreeSet<OrderData>> entry = iterator.next();
                    BigDecimal price = entry.getKey().setScale(precision - i, RoundingMode.DOWN);
                    DepthData depthData = depthDataMap.get(String.valueOf(i));
                    List<List<BigDecimal>> bids = depthData.getBids();

                    if (bids.size() == 0 || bids.get(bids.size() - 1).get(0).compareTo(price) != 0) {

                        if (bids.size() > 20) {
                            break;
                        }

                        bids.add(new ArrayList<BigDecimal>() {
                            {
                                add(price);
                                add(entry.getValue().stream().map(it -> it.getAmount().subtract(it.getFieldAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                        });
                    } else {
                        bids.get(bids.size() - 1).set(1, entry.getValue().stream().map(it -> it.getAmount().subtract(it.getFieldAmount())).reduce(BigDecimal.ZERO, BigDecimal::add).add(bids.get(bids.size() - 1).get(1)));
                    }
                }
            }
        }

        if (orderSellPriceQueueTreeMap != null && orderSellPriceQueueTreeMap.size() > 0) {
            Set<Map.Entry<BigDecimal, TreeSet<OrderData>>> sellEntries = orderSellPriceQueueTreeMap.entrySet();
            for (int i = 0; i < 6; i++) {
                Iterator<Map.Entry<BigDecimal, TreeSet<OrderData>>> iterator = sellEntries.iterator();
                while (iterator.hasNext()) {

                    Map.Entry<BigDecimal, TreeSet<OrderData>> entry = iterator.next();
                    BigDecimal price = entry.getKey().setScale(precision - i, RoundingMode.DOWN);
                    DepthData depthData = depthDataMap.get(String.valueOf(i));
                    List<List<BigDecimal>> asks = depthData.getAsks();
                    if (asks.size() == 0 || asks.get(asks.size() - 1).get(0).compareTo(price) != 0) {
                        if (asks.size() > 20) {
                            break;
                        }
                        asks.add(new ArrayList<BigDecimal>() {
                            {
                                add(price);
                                add(entry.getValue().stream().map(it -> it.getAmount().subtract(it.getFieldAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
                            }
                        });
                    } else {
                        asks.get(asks.size() - 1).set(1, entry.getValue().stream().map(it -> it.getAmount().subtract(it.getFieldAmount())).reduce(BigDecimal.ZERO, BigDecimal::add).add(asks.get(asks.size() - 1).get(1)));
                    }
                }
            }
        }

        return depthDataMap;
    }


    /**
     * 获取买一卖一逐笔详情
     *
     * @param symbolId
     * @return
     */
    public TickData getTickData(Long symbolId) {

        TreeMap<BigDecimal, TreeSet<OrderData>> orderBuyPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(symbolId, true));
        TreeMap<BigDecimal, TreeSet<OrderData>> orderSellPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(symbolId, false));

        TickData tickData = new TickData();
        tickData.setQuoteTime(System.currentTimeMillis());
        if (orderBuyPriceQueueTreeMap != null) {
            Map.Entry<BigDecimal, TreeSet<OrderData>> entry = orderBuyPriceQueueTreeMap.lastEntry();
            if (entry == null) {
                tickData.setBid(null);
                tickData.setBidSize(BigDecimal.ZERO);
            } else {
                tickData.setBid(entry.getKey());
                tickData.setBidSize(entry.getValue().stream().map(it -> it.getAmount().subtract(it.getFieldAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
            }
        }


        if (orderSellPriceQueueTreeMap != null) {
            Map.Entry<BigDecimal, TreeSet<OrderData>> entry = orderSellPriceQueueTreeMap.firstEntry();
            if (entry == null) {
                tickData.setAsk(null);
                tickData.setAskSize(BigDecimal.ZERO);
            } else {
                tickData.setAsk(entry.getKey());
                tickData.setAskSize(entry.getValue().stream().map(it -> it.getAmount().subtract(it.getFieldAmount())).reduce(BigDecimal.ZERO, BigDecimal::add));
            }
        }

        return tickData;
    }

    /**
     * 获取订单簿中的订单
     * @param orderData
     * @return
     */
    public OrderData getOrder(OrderData orderData) {

        TreeMap<BigDecimal, TreeSet<OrderData>> orderPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(orderData.getSymbolId(), OrderType.buy(orderData.getType())));
        if (orderPriceQueueTreeMap == null) {
            return null;
        }
        TreeSet<OrderData> orderList = orderPriceQueueTreeMap.get(orderData.getPrice());
        if (orderList == null) {
            return null;
        } else {
            OrderData ceiling = orderList.ceiling(orderData);
            return ceiling;
        }
    }


    /**
     * 取消订单并移除
     *
     * @param orderData
     */
    public OrderData cancelOrder(OrderData orderData) {
        OrderData order = getOrder(orderData);
        if (order != null && removeOrder(orderData)) {
            return order;
        }
        return null;
    }


    /**
     * 处理订单的状态
     *
     * @param orderData
     */
    private void handleOrderState(OrderData orderData) {
        if (orderData.getFieldAmount().compareTo(orderData.getAmount()) >= 0) {
            orderData.setState(OrderState.filled);
        } else if (orderData.getFieldAmount().compareTo(orderData.getAmount()) < 0) {
            orderData.setState(OrderState.partial_filled);
        } else if (orderData.getFieldAmount().compareTo(BigDecimal.ZERO) == 0) {
            orderData.setState(OrderState.submitted);
        }
    }


    /**
     * 添加订单到订单池
     *
     * @param orderData
     */
    private void addOrder(OrderData orderData) {
        TreeMap<BigDecimal, TreeSet<OrderData>> orderPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(orderData.getSymbolId(), OrderType.buy(orderData.getType())));
        if (orderPriceQueueTreeMap == null) {
            orderPriceQueueTreeMap = new TreeMap<>();
            symbolOrderPriceListHashMap.put(OrderTool.getOrderPriceListKey(orderData.getSymbolId(), OrderType.buy(orderData.getType())), orderPriceQueueTreeMap);
        }

        TreeSet<OrderData> orderList = orderPriceQueueTreeMap.get(orderData.getPrice());
        if (orderList == null) {
            orderList = new TreeSet<>();
            orderPriceQueueTreeMap.put(orderData.getPrice(), orderList);
        }

        if (orderList.contains(orderData)) {
            log.error("订单在内存中已经存在 inputOrder {}", JSON.toJSONString(orderData));
            throw new ErrorMsgException("订单已经存在");
        }

        boolean b = orderList.add(OrderTool.copyOrderData(orderData));
        if (b == false) {
            log.error("添加订单到队列失败 inputOrder {}", JSON.toJSONString(orderData));
            throw new ErrorMsgException("插入失败 " + JSON.toJSONString(orderData));
        } else {
            orderEvent.addOrder(OrderTool.copyOrderData(orderData));
        }
    }


    private boolean removeOrder(OrderData orderData) {
        TreeMap<BigDecimal, TreeSet<OrderData>> orderPriceQueueTreeMap = symbolOrderPriceListHashMap.get(OrderTool.getOrderPriceListKey(orderData.getSymbolId(), OrderType.buy(orderData.getType())));
        if (orderPriceQueueTreeMap == null) {
            return false;
        }

        TreeSet<OrderData> orderList = orderPriceQueueTreeMap.get(orderData.getPrice());
        if (orderList == null) {
            return false;
        } else {
            boolean b = orderList.remove(orderData);
            if (b && orderList.size() == 0) {
                orderPriceQueueTreeMap.remove(orderData.getPrice());
            }
            return b;
        }
    }


    private boolean removeOrder(OrderData orderData, TreeSet<OrderData> toOrderDataTreeSet, Iterator<BigDecimal> priceIterator) {
        TreeSet<OrderData> orderList = toOrderDataTreeSet;
        if (orderList == null) {
            return false;
        } else {
            boolean b = orderList.remove(orderData);
            if (b && orderList.size() == 0) {
                priceIterator.remove();
            }
            return b;
        }
    }


}
