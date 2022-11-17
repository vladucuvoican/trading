package com.example.trading.model.dto;

public class OrderDto {

    private long id;

    private double price;

    private String side;

    private long size;

    public OrderDto(long id, double price, char side, long size) {
        this.id = id;
        this.price = price;
        this.size = size;
        this.side = "" + side;
    }

    public long getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public long getSize() {
        return size;
    }

    public String getSide() {
        return side;
    }
}
