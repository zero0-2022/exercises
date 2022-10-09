package com.test.mycat;

import com.test.servlet.MyRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

/**
 * MyCat 对 MyRequest 的默认实现
 */
public class HttpMyRequest implements MyRequest {
    private final HttpRequest nettyRequest;

    public HttpMyRequest(HttpRequest request) {
        this.nettyRequest = request;
    }

    @Override
    public String getUri() {
        return this.nettyRequest.uri();
    }

    @Override
    public String getPath() {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(this.getUri());
        return queryDecoder.path();
    }

    @Override
    public String getMethod() {
        return this.nettyRequest.method().name();
    }

    @Override
    public Map<String, List<String>> getParams() {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(this.getUri());
        return queryDecoder.parameters();
    }

    @Override
    public List<String> getParams(String name) {
        return getParams().get(name);
    }

    @Override
    public String getParam(String name) {
        List<String> params = getParams(name);
        if (params == null || params.size() == 0) {
            return null;
        }
        return params.get(0);
    }
}
