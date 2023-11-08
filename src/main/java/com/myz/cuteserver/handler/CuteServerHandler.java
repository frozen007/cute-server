package com.myz.cuteserver.handler;

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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: zhaomingyu
 * @date: 2023/11/8 11:36 AM
 * @description:
 */
public class CuteServerHandler extends ChannelInboundHandlerAdapter {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private HttpRequest request;

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
        String response;
        switch (uri) {
            case "/hello":
                response = "{\"msg\":\"hi, this is cute server\"}";
                break;
            case "/time":
                Date now = new Date();
                response = "{\"msg\":\"hi, server time: " + sdf.format(now)+"\"}";
                break;
            default:
                response = "{\"msg\":\"unknown uri " + uri + "\"}";
                break;
        }

        byte[] bytes = response.getBytes("UTF-8");
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(bytes));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        if (HttpUtil.isKeepAlive(request)) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.CONNECTION);
        }
        ctx.channel().writeAndFlush(httpResponse);
    }
}
