package com.hfs.libnetwork;

/**
 * 统一返回参数
 */
public class ApiResponse<T> {
    public boolean success;
    public int status;
    public String message;
    public T body;
}
