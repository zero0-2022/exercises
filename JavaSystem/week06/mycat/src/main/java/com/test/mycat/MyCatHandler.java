package com.test.mycat;

import com.test.servlet.MyRequest;
import com.test.servlet.MyResponse;
import com.test.servlet.MyServlet;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import javax.activation.MimetypesFileTypeMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyCatHandler extends ChannelInboundHandlerAdapter {
    private Map<String, MyServlet> nameServletMap = new ConcurrentHashMap<>();
    private Map<String, String> nameServletClassMap = new HashMap<>();
    private Map<String, String> staticSources = new HashMap<>();
    private final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

    public MyCatHandler(Map<String, MyServlet> nameServletMap, Map<String, String> nameClassMap, Map<String, String> staticSources) {
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
        if (servlet instanceof DefaultServlet) {
            handleStaticSource(ctx, request, uri);
            return;
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

    public void handleStaticSource(ChannelHandlerContext ctx, HttpRequest request, String uri) throws IOException {
        if(!uri.contains("/")) {
            return;
        }
        String sourceName = uri.substring(uri.lastIndexOf("/") + 1);
        String path = staticSources.get(sourceName);
        if (path == null) {
            return;
        }
        staticContentWrite(ctx, request, path, sourceName);
    }

    public void staticContentWrite(ChannelHandlerContext ctx, HttpRequest request, String path, String sourceName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(path);
        FileChannel fileChannel = fileInputStream.getChannel();

        // 读取文件到 buffer 缓存
        long min = Math.min(fileChannel.size(), 1024);
        ByteBuffer byteBuffer = ByteBuffer.allocate((int)min);
        int read;
        while (true) {
            byteBuffer.clear();
            read = fileChannel.read(byteBuffer);
            if (read == -1){
                break;
            }
            byteBuffer.flip();
        }
        // 创建响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(byteBuffer)
        );
        // 获取响应头
        HttpHeaders headers = response.headers();
        // 设置响应体类型
        String mimeType = mimetypesFileTypeMap.getContentType(path);
        String fileType = mimeType.equals("application/octet-stream") ? "text/html": mimeType;
        if (sourceName.endsWith(".zip") || sourceName.endsWith(".rar") || sourceName.endsWith(".tar")) {
            fileType = "application/zip";
        }
        headers.set(HttpHeaderNames.CONTENT_TYPE, fileType);
        // 设置响应长度
        headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 设置缓存过期时间
        headers.set(HttpHeaderNames.EXPIRES, 0);
        // 若 HTTP 请求是长连接，则相应也用长连接
        if (HttpUtil.isKeepAlive(request)) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
        ctx.close();
    }
}
