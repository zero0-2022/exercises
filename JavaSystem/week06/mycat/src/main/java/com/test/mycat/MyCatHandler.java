package com.test.mycat;

import com.test.servlet.MyRequest;
import com.test.servlet.MyResponse;
import com.test.servlet.MyServlet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;

import javax.activation.MimetypesFileTypeMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MyCatHandler extends ChannelInboundHandlerAdapter {
    private Map<String, MyServlet> nameServletMap = new ConcurrentHashMap<>();
    private Map<String, String> nameServletClassMap = new HashMap<>();
    private Set<String> staticSources = new HashSet<>();
    private final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

    public MyCatHandler(Map<String, MyServlet> nameServletMap, Map<String, String> nameClassMap, Set<String> staticSources) {
        this.nameServletMap = nameServletMap;
        this.nameServletClassMap = nameClassMap;
        this.staticSources = staticSources;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            return;
        }
        HttpRequest request = (HttpRequest) msg;
        String uri = request.uri();
        // 解析出请求 servlet 资源
        String servletName = "";
        int end = 0;
        if (uri.contains("?") && uri.contains("/")) {
            end = uri.indexOf("?");
            if (uri.contains(".")) {
                end = uri.indexOf(".");
            }
        } else if(uri.contains(".") && uri.contains("/")) {
            end =  uri.indexOf(".");
        } else if (!uri.contains("?") && uri.contains("/")) {
            if (uri.contains(".")) {
                end =  uri.indexOf(".");
            } else {
                end = uri.length();
            }
        }
        if (end != 0) {
            servletName = uri.substring(uri.lastIndexOf("/") + 1, end);
        }

        MyServlet servlet = new DefaultServlet();
        if (nameServletMap.containsKey(servletName)) {
            servlet = nameServletMap.get(servletName);
        } else if (nameServletClassMap.containsKey(servletName)) {
            if (nameServletMap.get(servletName) == null) {
                synchronized (this) {
                    if (nameServletMap.get(servletName) == null) {
                        String className = nameServletClassMap.get(servletName);
                        servlet = (MyServlet) Class.forName(className).getDeclaredConstructor().newInstance();
                        nameServletMap.put(servletName, servlet);
                    }
                }
            }
        }
        // 访问静态资源
        if (servlet instanceof DefaultServlet) {
        }

        MyRequest myRequest = new HttpMyRequest(request);
        MyResponse myResponse = new HttpMyResponse(request, ctx);
        String method = myRequest.getMethod();
        if (method.equalsIgnoreCase("GET")) {
            servlet.doGet(myRequest, myResponse);
        } else if(method.equalsIgnoreCase("POST")) {
            servlet.doPost(myRequest, myResponse);
        }
        ctx.close();
    }
}
