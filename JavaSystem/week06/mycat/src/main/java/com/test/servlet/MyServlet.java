package com.test.servlet;

/**
 * Servlet 规范
 */
public abstract class MyServlet {
    public abstract void doGet(MyRequest request, MyResponse response) throws Exception;
    public abstract void doPost(MyRequest request, MyResponse response) throws Exception;
}
