package com.test.mycat;

import com.test.servlet.MyResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;

import java.nio.charset.StandardCharsets;

public class HttpMyResponse implements MyResponse {
    private final HttpRequest request;
    private final ChannelHandlerContext context;

    public HttpMyResponse(HttpRequest request, ChannelHandlerContext context) {
        this.request = request;
        this.context = context;
    }

    @Override
    public void write(String content) throws Exception {
        if (StringUtil.isNullOrEmpty(content)) {
            return;
        }

        // 创建响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content.getBytes(StandardCharsets.UTF_8)) // 根据响应数据大小为 response 对象分配存储空间
                );
        // 获取响应头
        HttpHeaders headers = response.headers();
        // 设置响应体类型
        headers.set(HttpHeaderNames.CONTENT_TYPE, "text/json");
        // 设置响应长度
        headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 设置缓存过期时间
        headers.set(HttpHeaderNames.EXPIRES, 0);
        // 若 HTTP 请求是长连接，则相应也用长连接
        if (HttpUtil.isKeepAlive(request)) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        context.writeAndFlush(response);
    }
}
