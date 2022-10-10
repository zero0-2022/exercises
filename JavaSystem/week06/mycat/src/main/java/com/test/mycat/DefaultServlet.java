package com.test.mycat;

import com.test.servlet.MyRequest;
import com.test.servlet.MyResponse;
import com.test.servlet.MyServlet;

/**
 * Servlet 规范默认实现
 */
public class DefaultServlet extends MyServlet {
    @Override
    public void doGet(MyRequest request, MyResponse response) throws Exception {
        response.write("404 Not Found");
    }

    @Override
    public void doPost(MyRequest request, MyResponse response) throws Exception {
        doGet(request, response);
    }
}
