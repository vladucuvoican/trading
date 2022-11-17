package com.example.trading.model.dto;

import java.util.ArrayList;
import java.util.List;

public class ApiResponseDto<T> {

    private T data;

    private List<String> info = new ArrayList<>(0);

    private List<String> errors = new ArrayList<>(0);

    private List<String> warnings = new ArrayList<>(0);

    public ApiResponseDto() {
    }

    public ApiResponseDto(T data) {
        this.data = data;
    }

    public static <T> ApiResponseDto<T> build(T data) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.data = data;
        return response;
    }

    public static <T> ApiResponseDto<T> createApiResponseWithErrors(T data, String error) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.data = data;
        response.errors = new ArrayList<>();
        response.errors.add(error);
        return response;
    }

    public static <T> ApiResponseDto<T> createApiResponseWithErrors(T data, List<String> errors) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.data = data;
        response.errors = errors;
        return response;
    }

    public static <T> ApiResponseDto<T> createApiResponseWithWarnings(T data, List<String> warnings) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.data = data;
        response.warnings = warnings;
        return response;
    }

    public T getData() {
        return data;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getInfo() {
        return info;
    }

    public List<String> getErrors() {
        return errors;
    }

}