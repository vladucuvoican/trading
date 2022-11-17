package com.example.trading.model.dto;


import java.util.StringJoiner;

public class OrderAddRequestDto {

    private Long id;

    private Double price;

    private String side;

    private Long size;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrderAddRequestDto.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("price=" + price)
                .add("side=" + side)
                .add("size=" + size)
                .toString();
    }
}
