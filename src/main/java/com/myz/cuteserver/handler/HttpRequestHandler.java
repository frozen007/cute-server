package com.myz.cuteserver.handler;

import com.myz.cuteserver.processor.HttpRequestProcessor;
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

/**
 * @author: zhaomingyumt
 * @date: 2023/11/25 11:59 AM
 * @description:
 */
public class HttpRequestHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    private HttpRequestProcessor processor;

    private HttpRequest request;

    public HttpRequestHandler(HttpRequestProcessor processor) {
        this.processor = processor;
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

    private void handle(ChannelHandlerContext ctx, HttpRequest request) throws UnsupportedEncodingException {
        Object result = null;
        try {
            result = processor.processHttpRequest(request);
        } catch (NoSuchMethodException e) {
            ctx.channel().writeAndFlush(returnWithStatus(e.getMessage(), HttpResponseStatus.NOT_FOUND));
            return;
        } catch (Exception e) {
            logger.error("exception when processHttpRequest", e);
            ctx.channel().writeAndFlush(returnWithStatus(e.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR));
            return;
        }

        String response = result == null ? "null" : result.toString();

        HttpResponseStatus status = HttpResponseStatus.OK;

        FullHttpResponse httpResponse;
        byte[] bytes = new byte[0];
        try {
            bytes = response.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(bytes));

        //todo: may be controlled by outer code
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        if (HttpUtil.isKeepAlive(request)) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.CONNECTION);
        }

        ctx.channel().writeAndFlush(httpResponse);
    }

    private FullHttpResponse returnWithStatus(String msg, HttpResponseStatus status) throws UnsupportedEncodingException {
        if (msg == null) {
            msg = "null";
        }
        byte[] bytes = msg.getBytes("UTF-8");
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(bytes));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        return httpResponse;
    }

}
