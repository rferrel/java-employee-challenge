package com.reliaquest.api.model;

public class ApiResponse<T> {
    private T data;
    private String status;

    public ApiResponse() {}

    public ApiResponse(T data, String status) {
        this.data = data;
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
