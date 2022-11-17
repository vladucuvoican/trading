package com.example.trading.model.enums;

public enum OrderSideType {
    BID ('B'), OFFER ('O');

    private char side;

    OrderSideType(char side) {
        this.side = side;
    }

    public char getSide() {
        return side;
    }
}
