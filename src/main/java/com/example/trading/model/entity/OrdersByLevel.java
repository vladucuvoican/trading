package com.example.trading.model.entity;

import java.util.List;

public class OrdersByLevel {

    private List<Order> bidOrders;
    private List<Order> offerOrders;

    public OrdersByLevel(List<Order> bidOrders, List<Order> offerOrders) {
        this.bidOrders = bidOrders;
        this.offerOrders = offerOrders;
    }

    public double getBidPrice() {
        return bidOrders.get(0).getPrice();
    }

    public long getBidTotalSize() {
        return bidOrders.stream().map(Order::getSize).reduce(0L, Long::sum);
    }

    public double getOfferPrice() {
        return offerOrders.get(0).getPrice();
    }

    public long getOfferTotalSize() {
        return offerOrders.stream().map(Order::getSize).reduce(0L, Long::sum);
    }

    public List<Order> getBidOrders() {
        return bidOrders;
    }

    public List<Order> getOfferOrders() {
        return offerOrders;
    }
}
