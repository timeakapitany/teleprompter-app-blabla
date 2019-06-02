package com.example.blabla.util;

public class Result<T> {

    private boolean isSuccess;
    private Exception ex;
    private T data;

    private Result() {
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.isSuccess = true;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(Exception ex) {
        Result<T> result = new Result<>();
        result.isSuccess = false;
        result.ex = ex;
        return result;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public T getData() {
        return data;
    }

    public Exception getException() {
        return ex;
    }
}
