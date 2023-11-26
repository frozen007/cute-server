package com.myz.cuteserver.processor;

import io.netty.handler.codec.http.HttpRequest;

/**
 * @author: zhaomingyu
 * @description:
 */
public interface HttpRequestProcessor {
    Object processHttpRequest(HttpRequest request) throws Exception;

}
