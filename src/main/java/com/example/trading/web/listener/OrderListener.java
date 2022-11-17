package com.example.trading.web.listener;

import com.example.trading.model.dto.OrderAddRequestDto;
import com.example.trading.model.dto.OrderDeleteRequestDto;
import com.example.trading.service.OrderBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private OrderBookService orderBookService;

    public OrderListener(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @JmsListener(destination = "AddOrderQueue", containerFactory = "myJmsFactory")
    public void receiveAddOrderMessage(OrderAddRequestDto orderAddRequestDto) {
        logger.info("Calling method ---- receiveAddOrderMessage ---");
        orderBookService.addOrder(orderAddRequestDto);
    }

    @JmsListener(destination = "RemoveOrderQueue", containerFactory = "myJmsFactory")
    public void receiveOrderDeleteMessage(OrderDeleteRequestDto orderDeleteRequestDto) {
        logger.info("Calling method ---- receiveOrderDeleteMessage ---");
        orderBookService.removeOrder(orderDeleteRequestDto);
    }
}
