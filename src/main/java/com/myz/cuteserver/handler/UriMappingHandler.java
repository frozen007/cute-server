package com.myz.cuteserver.handler;

import com.myz.cuteserver.MethodHandle;
import com.myz.cuteserver.annotation.UriMapping;
import com.myz.cuteserver.annotation.UriVariable;
import com.myz.cuteserver.mapping.RootMapping;
import com.myz.cuteserver.mapping.SimpleMapping;
import com.myz.cuteserver.util.StrUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zhaomingyumt
 * @date: 2023/11/24 7:23 PM
 * @description:
 */
public class UriMappingHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(UriMappingHandler.class);

    private HttpRequest request;

    List mappingObjList = new ArrayList();

    Map<String, MethodHandle> methodHandleMap = new HashMap<>();

    public UriMappingHandler() {
        mappingObjList.add(new SimpleMapping());

        for (Object mappingObj : mappingObjList) {
            findMappingMethods(mappingObj);
        }

        if (!methodHandleMap.containsKey("/")) {
            findMappingMethods(new RootMapping());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
        }

        if (msg instanceof HttpContent) {
            if ("/favicon.ico".equals(request.uri())) {
                return;
            }

            if (msg instanceof LastHttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                byte[] buffer = new byte[buf.readableBytes()];
                buf.readBytes(buffer);
                buf.release();
                handle(ctx, request);
            }
        }
    }

    private void handle(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        String uri = request.uri();
        if (StrUtil.isEmpty(uri)) {
            logger.info("bad request: uri is empty");
            ctx.channel().writeAndFlush(returnWithStatus("uri is empty", HttpResponseStatus.BAD_REQUEST));
            return;
        }
        logger.info("incoming request: uri={}", uri);

        int tag = uri.indexOf("?");
        String path;
        if (tag < 0) {
            path = uri;
        } else {
            path = uri.substring(0, tag);
        }

        MethodHandle methodHandle = methodHandleMap.get(path);
        if (methodHandle == null) {
            logger.info("bad request: can not find path {}", path);
            ctx.channel().writeAndFlush(returnWithStatus("path not found", HttpResponseStatus.NOT_FOUND));
            return;
        }

        String[] params = methodHandle.getParams();
        Object[] args = new Object[0];
        if (params != null) {
            args = new Object[params.length];
            if (uri.length() > tag + 1) {
                String varStr = uri.substring(tag + 1);
                Map<String, Object> paraMap = StrUtil.parseQueryString(varStr, "utf-8");
                for (int i = 0; i < params.length; i++) {
                    String[] values = (String[]) paraMap.get(params[i]);
                    args[i] = values[0];
                }
            }
        }

        Object result = methodHandle.doInvoke(args);

        String response = result.toString();

        HttpResponseStatus status = HttpResponseStatus.OK;

        FullHttpResponse httpResponse;
        byte[] bytes = response.getBytes("UTF-8");
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(bytes));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        if (HttpUtil.isKeepAlive(request)) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.CONNECTION);
        }

        ctx.channel().writeAndFlush(httpResponse);
    }

    private void findMappingMethods(Object mappingObj) {
        Method[] methods = mappingObj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            UriMapping uriMapping = method.getAnnotation(UriMapping.class);
            if (uriMapping == null) {
                continue;
            }

            Parameter[] parameters = method.getParameters();
            String[] params = null;
            if (parameters != null) {
                params = new String[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    UriVariable uriVariable = parameter.getAnnotation(UriVariable.class);
                    String paramName;
                    if (uriVariable == null || StrUtil.isEmpty(uriVariable.value())) {
                        paramName = parameter.getName();
                    } else {
                        paramName = uriVariable.value();
                    }

                    params[i] = paramName;
                }
            }

            String path = uriMapping.value();
            if (!StrUtil.isEmpty(path)) {
                methodHandleMap.put(path, new MethodHandle(method, mappingObj, params));
            }
        }
    }

    private FullHttpResponse returnWithStatus(String msg, HttpResponseStatus status) throws UnsupportedEncodingException {
        byte[] bytes = msg.getBytes("UTF-8");
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(bytes));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        return httpResponse;
    }
}
