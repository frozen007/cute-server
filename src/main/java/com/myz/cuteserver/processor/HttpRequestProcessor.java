package com.myz.cuteserver.processor;

import io.netty.handler.codec.http.HttpRequest;

/**
 * @author: zhaomingyumt
 * @date: 2023/11/25 12:10 PM
 * @description:
 */
public interface HttpRequestProcessor {
    Object processHttpRequest(HttpRequest request) throws Exception;

}
