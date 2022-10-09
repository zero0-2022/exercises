package com.testapp.webapp;

import com.test.servlet.MyRequest;
import com.test.servlet.MyResponse;
import com.test.servlet.MyServlet;

public class MyServletDemo extends MyServlet {

    @Override
    public void doGet(MyRequest myRequest, MyResponse myResponse) throws Exception {
        myResponse.write("MyCat Server Response");
    }

    @Override
    public void doPost(MyRequest myRequest, MyResponse myResponse) throws Exception {
        doGet(myRequest, myResponse);
    }
}
