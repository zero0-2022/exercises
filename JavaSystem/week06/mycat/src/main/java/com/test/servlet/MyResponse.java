package com.test.servlet;

/**
 * Servlet 响应规范
 */
public interface MyResponse {
    // 将响应数据写入到 Channel
    void write(String content) throws Exception;
}
