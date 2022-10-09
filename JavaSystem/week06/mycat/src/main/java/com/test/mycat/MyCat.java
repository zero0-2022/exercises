package com.test.mycat;

public class MyCat {
    public static void run(String[] args) throws Exception {
        MyCatServer server = new MyCatServer("com.testapp.webapp", "com.testapp.static");
        server.start();
    }
}
