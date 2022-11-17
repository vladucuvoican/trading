package com.example.trading.service;

import com.example.trading.model.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class OrderBookTest {

    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook();
    }

    @Test
    void testGetPrice() throws InterruptedException {
        // Given
        createOrdersUsingExecutorService();

        // When
        Double bidPriceLevel1 = orderBook.getPrice('B', 1);
        Double offerPriceLevel1 = orderBook.getPrice('O', 1);

        Double bidPriceLevel3 = orderBook.getPrice('B', 3);
        Double offerPriceLevel3 = orderBook.getPrice('O', 3);

        // Then
        assertThat(bidPriceLevel1).isNotNull();
        assertThat(bidPriceLevel1).isEqualTo(99.99);

        assertThat(offerPriceLevel1).isNotNull();
        assertThat(offerPriceLevel1).isEqualTo(100.00);

        assertThat(bidPriceLevel3).isNotNull();
        assertThat(bidPriceLevel3).isEqualTo(99.97);

        assertThat(offerPriceLevel3).isNotNull();
        assertThat(offerPriceLevel3).isEqualTo(100.02);
    }

    @Test
    void testThatWhenAddingAndRemoveingOrdersTheLevelWillChanges() throws InterruptedException {
        // Given
        createOrdersUsingExecutorService();

        // When
        Double bidPriceLevel1 = orderBook.getPrice('B', 1);

        assertThat(bidPriceLevel1).isNotNull();
        assertThat(bidPriceLevel1).isEqualTo(99.99);

        orderBook.removeOrder(1L);

        bidPriceLevel1 = orderBook.getPrice('B', 1);

        // Then
        assertThat(bidPriceLevel1).isNotNull();
        assertThat(bidPriceLevel1).isEqualTo(99.98);
    }

    @Test
    void testGetTotalSize() throws InterruptedException {
        // Given
        createOrdersUsingExecutorService();

        // When
        Long bidSizeLevel1 = orderBook.getTotalSize('B', 1);
        Long offerSizeLevel1 = orderBook.getTotalSize('O', 1);

        Long bidSizeLevel3 = orderBook.getTotalSize('B', 3);
        Long offerSizeLevel3 = orderBook.getTotalSize('O', 3);

        // Then
        assertThat(bidSizeLevel1).isNotNull();
        assertThat(bidSizeLevel1).isEqualTo(80);

        assertThat(offerSizeLevel1).isNotNull();
        assertThat(offerSizeLevel1).isEqualTo(175);

        assertThat(bidSizeLevel3).isNotNull();
        assertThat(bidSizeLevel3).isEqualTo(220);

        assertThat(offerSizeLevel3).isNotNull();
        assertThat(offerSizeLevel3).isEqualTo(390);
    }

    @Test
    void testGetOrdersBySideInLevelAndTimeOrdered() throws InterruptedException {
        // Given
        createOrdersUsingExecutorService();
        orderBook.addOrder(new Order(55, 100.00d, 'O', 175));

        // When
        List<Order> bidOrders = orderBook.getOrdersBySideInLevelAndTimeOrdered('B');
        List<Order> offerOrders = orderBook.getOrdersBySideInLevelAndTimeOrdered('O');
        long totalSize = orderBook.getTotalSize('O', 1);

        // Then
        assertThat(bidOrders).isNotNull();
        assertThat(bidOrders.stream().map(Order::getId).collect(Collectors.toList())).isEqualTo(Arrays.asList(1L, 3L, 2L, 4L));
        assertThat(offerOrders).isNotNull();
        assertThat(offerOrders.stream().map(Order::getId).collect(Collectors.toList())).isEqualTo(Arrays.asList(52L, 55L, 51L, 53L, 54L));
        assertThat(totalSize).isEqualTo(350);
    }

    @Test
    void testGetOrdersBySideInLevelAndTimeOrderedWhenDoingUpdate() throws InterruptedException {
        // Given
        createOrdersUsingExecutorService();
        orderBook.addOrder(new Order(55, 100.00d, 'O', 175));
        orderBook.updateOrder(55,  275);

        // When
        List<Order> bidOrders = orderBook.getOrdersBySideInLevelAndTimeOrdered('B');
        List<Order> offerOrders = orderBook.getOrdersBySideInLevelAndTimeOrdered('O');
        long totalSize = orderBook.getTotalSize('O', 1);

        // Then
        assertThat(bidOrders).isNotNull();
        assertThat(bidOrders.stream().map(Order::getId).collect(Collectors.toList())).isEqualTo(Arrays.asList(1L, 3L, 2L, 4L));
        assertThat(offerOrders).isNotNull();
        assertThat(offerOrders.stream().map(Order::getId).collect(Collectors.toList())).isEqualTo(Arrays.asList(52L, 55L, 51L, 53L, 54L));
        assertThat(totalSize).isEqualTo(450);
    }

    private void createOrdersUsingExecutorService() throws InterruptedException {
        List<Order> bidList = new ArrayList<>();
        List<Order> offerList = new ArrayList<>();

        bidList.add(new Order(1, 99.99d, 'B', 80));
        bidList.add(new Order(2, 99.97d, 'B', 220));
        bidList.add(new Order(3, 99.98d, 'B', 175));
        bidList.add(new Order(4, 99.96d, 'B', 500));

        offerList.add(new Order(51, 100.01d, 'O', 200));
        offerList.add(new Order(52, 100.00d, 'O', 175));
        offerList.add(new Order(53, 100.02d, 'O', 390));
        offerList.add(new Order(54, 100.03d, 'O', 360));


        ExecutorService executorService = Executors.newFixedThreadPool(4);

        bidList.forEach(order ->
                executorService.execute(() -> orderBook.addOrder(order))
        );

        offerList.forEach(order ->
                executorService.execute(() -> orderBook.addOrder(order))
        );

        executorService.shutdown();

        executorService.awaitTermination(60_000L, TimeUnit.MILLISECONDS);
    }
}
