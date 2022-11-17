package com.example.trading.service.mapper;

import com.example.trading.model.dto.OrderAddRequestDto;
import com.example.trading.model.dto.OrderDto;
import com.example.trading.model.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order mapToOrder(OrderAddRequestDto orderAddRequestDto) {
        return new Order(
                orderAddRequestDto.getId(),
                orderAddRequestDto.getPrice(),
                orderAddRequestDto.getSide().charAt(0),
                orderAddRequestDto.getSize()
        );
    }

    public List<OrderDto> mapToOrderDtos(List<Order> orders) {
        return orders.stream().map(this::mapToOrderDto).collect(Collectors.toList());
    }

    public OrderDto mapToOrderDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getPrice(),
                order.getSide(),
                order.getSize()
        );
    }
}
