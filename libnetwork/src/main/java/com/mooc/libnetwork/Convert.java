package com.mooc.libnetwork;

import java.lang.reflect.Type;

/**
 * 转化类
 */
public interface Convert<T> {
    T convert(String response, Type type);
}
