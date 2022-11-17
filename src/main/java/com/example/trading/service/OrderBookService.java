package com.example.trading.service;

import com.example.trading.model.dto.OrderAddRequestDto;
import com.example.trading.model.dto.OrderDeleteRequestDto;
import com.example.trading.model.dto.OrderDto;
import com.example.trading.model.entity.Order;
import com.example.trading.service.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;

@Service
public class OrderBookService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private OrderBook orderBook;
    private OrderMapper orderMapper;

    public OrderBookService(OrderBook orderBook, OrderMapper orderMapper) {
        this.orderBook = orderBook;
        this.orderMapper = orderMapper;
    }

    public void addOrder(OrderAddRequestDto orderAddRequestDto) {
        if(logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- addOrder --- for orderAddRequestDto : {}" , orderAddRequestDto));
        }

        Order newOrder = orderMapper.mapToOrder(orderAddRequestDto);

        orderBook.addOrder(newOrder);
    }

    public void removeOrder(OrderDeleteRequestDto orderDeleteRequestDto) {
        if(logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- removeOrder --- for orderDeleteRequestDto : {}" , orderDeleteRequestDto));
        }

        orderBook.removeOrder(orderDeleteRequestDto.getId());
    }

    public Double getPrice(char side, int levelId) {
        if(logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- getPrice --- for side : {} and level: {}" , side, levelId));
        }

        return orderBook.getPrice(side, levelId);
    }

    public Long getTotalSize(char side, int levelId) {
        if(logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- getTotalSize --- for side : {} and level: {}" , side, levelId));
        }

        return orderBook.getTotalSize(side, levelId);
    }

    public List<OrderDto> getOrdersBySideInLevelAndTimeOrdered(char side) {
        if(logger.isDebugEnabled()) {
            logger.debug(format("Calling method ---- getOrdersBySideInLevelAndTimeOrdered --- for side : {}" , side));
        }

        List<Order> orders = orderBook.getOrdersBySideInLevelAndTimeOrdered(side);

        return orderMapper.mapToOrderDtos(orders);
    }
}
