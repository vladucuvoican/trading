package com.example.trading.service;

import com.example.trading.model.entity.Order;
import com.example.trading.model.entity.OrdersByLevel;
import com.example.trading.model.enums.OrderSideType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.String.format;

@Component
public class OrderBook {
    private final Logger logger = LoggerFactory.getLogger(OrderBook.class);

    private Map<Long, Order> idToOrder = new ConcurrentHashMap<>();
    private Map<Double, List<Order>> priceToBidList = new ConcurrentHashMap<>();
    private Map<Double, List<Order>> priceToOfferList = new ConcurrentHashMap<>();

    private List<OrdersByLevel> bidToOfferByLevels = new ArrayList<>();
    private Comparator<Double> comparator = (Double::compareTo);
    private ConcurrentSkipListSet<Double> bidPriceSet = new ConcurrentSkipListSet<>(comparator.reversed());
    private ConcurrentSkipListSet<Double> offerPriceSet = new ConcurrentSkipListSet<>();

    public OrderBook() {
    }

    public void addOrder(Order order) {
        if (logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- addOrder --- for order : {}", order));
        }
        if (order.getSide() == OrderSideType.BID.getSide()) {
            List<Order> orderList = getBucketWithOrderList(priceToBidList, order.getPrice());
            orderList.add(order);
            bidPriceSet.add(order.getPrice());
        } else {
            List<Order> orderList = getBucketWithOrderList(priceToOfferList, order.getPrice());
            orderList.add(order);
            offerPriceSet.add(order.getPrice());
        }
        idToOrder.put(order.getId(), order);
        assignLevels();
    }

    public void removeOrder(long orderId) {
        if (logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- removeOrder --- for orderId : {}", orderId));
        }
        Order order = idToOrder.get(orderId);
        if (order == null) {
            return;
        }
        if (order.getSide() == OrderSideType.BID.getSide()) {
            List<Order> orderList = getBucketWithOrderList(priceToBidList, order.getPrice());
            orderList.remove(order);
            if(orderList.size() == 0) {
                bidPriceSet.remove(order.getPrice());
            }
        } else {
            List<Order> orderList = getBucketWithOrderList(priceToOfferList, order.getPrice());
            orderList.remove(order);
            if(orderList.size() == 0) {
                offerPriceSet.remove(order.getPrice());
            }
        }
        idToOrder.remove(orderId);
        assignLevels();
    }

    public void updateOrder(long orderId, long newSize) {
        if (logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- updateOrder --- for orderId : {} and size: {}", orderId, newSize));
        }
        Order order = idToOrder.get(orderId);
        if (order == null) {
            return;
        }
        Order newOrder = new Order(orderId, order.getPrice(), order.getSide(), newSize);
        List<Order> orderList;
        if (order.getSide() == OrderSideType.BID.getSide()) {
            orderList = getBucketWithOrderList(priceToBidList, order.getPrice());
        } else {
            orderList = getBucketWithOrderList(priceToOfferList, order.getPrice());
        }
        orderList.remove(order);
        orderList.add(newOrder);
        idToOrder.put(orderId, newOrder);
        // no need to modify the priceQueues since the prices remains the same
        assignLevels();
    }

    public Double getPrice(char side, int level) {
        if (logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- getPrice --- for side : {} and level: {}", side, level));
        }
        if(bidToOfferByLevels.size() == 0) {
            return null;
        }
        synchronized (bidToOfferByLevels) {
            OrdersByLevel ordersByLevel = bidToOfferByLevels.get(level - 1);
            if (side == OrderSideType.BID.getSide()) {
                return ordersByLevel.getBidPrice();
            }
            return  ordersByLevel.getOfferPrice();
        }
    }

    public Long getTotalSize(char side, int level) {
        if (logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- getTotalSize --- for side : {} and level: {}", side, level));
        }
        if(bidToOfferByLevels.size() == 0) {
            return null;
        }
        synchronized (bidToOfferByLevels) {
            OrdersByLevel ordersByLevel = bidToOfferByLevels.get(level - 1);
            if (side == OrderSideType.BID.getSide()) {
                return ordersByLevel.getBidTotalSize();
            }
            return  ordersByLevel.getOfferTotalSize();
        }
    }

    public List<Order> getOrdersBySideInLevelAndTimeOrdered(char side) {
        if (logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- getOrdersBySideInLevelAndTimeOrdered --- for side : {}", side));
        }
        List<Order> orders = new ArrayList<>();
        synchronized (bidToOfferByLevels) {
            for (OrdersByLevel ordersByLevel : bidToOfferByLevels) {
                if (side == OrderSideType.BID.getSide()) {
                    orders.addAll(ordersByLevel.getBidOrders());
                } else {
                    orders.addAll(ordersByLevel.getOfferOrders());
                }
            }
        }
        return orders;
    }

    /**
     * Returns the bucket from the provided map, or in case there isn't one it returns a new one ensuring
     * using the double-checked locking that no concurrent threads can create  lists simultaneous
     *
     * @param map   - provided map
     * @param price - key by which we search for the bucket
     * @return the existing list of order or an empty {@link CopyOnWriteArrayList}
     */
    private List<Order> getBucketWithOrderList(Map<Double, List<Order>> map, double price) {
        List<Order> bucket = map.get(price);
        if (bucket != null) {
            return bucket;
        }
        synchronized (map) {
            bucket = map.get(price);
            if (bucket == null) {
                // later on we will use list.iterator() to get a snapshot to compute the levels
                // In addition {@link CopyOnWriteArrayList} protects us when we do add or remove an element
                bucket = new CopyOnWriteArrayList<>();
                map.put(price, bucket);
            }
            return bucket;
        }
    }

    /**
     * assign levels based on price-time priority.
     */
    private synchronized void assignLevels() {
        List<OrdersByLevel> ordersByLevels = new ArrayList<>();

        // copy lists & maps in order to manipulate the elements references directly

        List<Double> bidPrices = new LinkedList<>(Arrays.asList(bidPriceSet.toArray(new Double[bidPriceSet.size()])));
        List<Double> offerPrices =  new LinkedList<>(Arrays.asList(offerPriceSet.toArray(new Double[offerPriceSet.size()])));

        Map<Double, List<Order>> priceToBidListMap = synchronizedCopyMap(priceToBidList);
        Map<Double, List<Order>> priceToOfferListMap = synchronizedCopyMap(priceToOfferList);

        while (true) {
            if (CollectionUtils.isEmpty(bidPrices) || CollectionUtils.isEmpty(offerPrices)) {
                synchronized (bidToOfferByLevels) {
                    bidToOfferByLevels.clear();
                    bidToOfferByLevels.addAll(ordersByLevels);
                }
                logger.info("assignLevels finished");
                return;
            }
            Double highestBid = bidPrices.get(0);
            Double lowestOffer = offerPrices.get(0);

            /// copy lists so we can modify them directly
            List<Order> bidList = priceToBidListMap.get(highestBid);
            List<Order> offerList = priceToOfferListMap.get(lowestOffer);

            if (CollectionUtils.isEmpty(bidList) || CollectionUtils.isEmpty(offerList)) {
                logger.info("assignLevels finished");
                synchronized (bidToOfferByLevels) {
                    bidToOfferByLevels.clear();
                    bidToOfferByLevels.addAll(ordersByLevels);
                }
                return;
            }

            ordersByLevels.add(new OrdersByLevel(bidList, offerList));

            bidPrices.remove(0);
            offerPrices.remove(0);
        }
    }

    /**
     * Returns a copy for the provided map by using synchronized blocks
     *
     * @param map - provided map
     * @return a new {@link HashMap} with the same values
     */
    private Map<Double, List<Order>> synchronizedCopyMap(Map<Double, List<Order>> map) {
        Map<Double, List<Order>> newMap = new HashMap<>();
        synchronized (map) {
            for (Map.Entry<Double, List<Order>> entry : map.entrySet()) {
                List<Order> listOrder = new LinkedList<>();
                entry.getValue().iterator().forEachRemaining(listOrder::add);
                newMap.put(entry.getKey(), listOrder);
            }
        }
        return newMap;
    }
}
