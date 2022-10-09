package com.test.servlet;

import java.util.List;
import java.util.Map;

/**
 * Servlet 请求规范
 */
public interface MyRequest {
    // 获取 URI，包含请求参数
    String getUri();
    // 获取请求路径
    String getPath();
    // 获取请求方法
    String getMethod();
    // 获取所有请求参数，以 Map 形式返回
    Map<String, List<String>> getParams();
    // 获取指定名称的请求参数列表
    List<String> getParams(String name);
    // 获取指定名称请求参数的第一个值
    String getParam(String name);
}
