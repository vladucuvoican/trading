package com.example.trading.web.controller;

import com.example.trading.model.dto.ApiResponseDto;
import com.example.trading.model.dto.OrderAddRequestDto;
import com.example.trading.model.dto.OrderDto;
import com.example.trading.service.OrderBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;

@Validated
@RestController("/api/v1")
public class OrderBookController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @PostMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addOrder(@RequestBody @Valid OrderAddRequestDto orderAddRequestDto) {
        logger.info("Calling method ---- addOrder ---");

        orderBookService.addOrder(orderAddRequestDto);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/orders/levels/{levelId}/price", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Double>> getPrice(
            @PathVariable @Valid @Min(1) int levelId,
            @RequestParam @Valid @Pattern(regexp = "^(B|O)$", message = "only `B` or `O` types are allowed") String side) {
        logger.info("Calling method ---- getPrice ---");

        Double price = orderBookService.getPrice(side.charAt(0), levelId);
        ApiResponseDto<Double> apiResponse;
        if(price == null) {
            apiResponse = ApiResponseDto.createApiResponseWithWarnings(null,
                    List.of("Couldn't compute price since there aren't sufficient offers and bids"));
        } else {
            apiResponse = ApiResponseDto.build(price);
        }
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/orders/levels/{levelId}/totalSize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Long>> getTotalSize(
            @PathVariable @Valid @Min(1) int levelId,
            @RequestParam @Valid @Pattern(regexp = "^(B|O)$", message = "only `B` or `O` types are allowed") String side) {
        logger.info("Calling method ---- getTotalSize ---");

        Long size = orderBookService.getTotalSize(side.charAt(0), levelId);
        ApiResponseDto<Long> apiResponse;
        if(size == null) {
            apiResponse = ApiResponseDto.createApiResponseWithWarnings(null,
                    List.of("Couldn't compute size since there aren't sufficient offers and bids"));
        } else {
            apiResponse = ApiResponseDto.build(size);
        }
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<List<OrderDto>>> getOrdersBySideInLevelAndTimeOrdered(
            @RequestParam @Valid @Pattern(regexp = "^(B|O)$", message = "only `B` or `O` types are allowed") String side) {
        logger.info("Calling method ---- getOrdersBySideInLevelAndTimeOrdered ---");

        List<OrderDto> orderDtos = orderBookService.getOrdersBySideInLevelAndTimeOrdered(side.charAt(0));

        return new ResponseEntity<>(ApiResponseDto.build(orderDtos), HttpStatus.OK);
    }
}
