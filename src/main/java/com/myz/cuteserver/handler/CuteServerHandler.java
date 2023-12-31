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
 */
@Deprecated
public class CuteServerHandler extends ChannelInboundHandlerAdapter {

    private static final String REDIRECT_URL = "";

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
        String response="";
        boolean isRedirect = false;
        String redirectUrl = "";
        HttpResponseStatus status = HttpResponseStatus.OK;
        switch (uri) {
            case "/hello":
                response = "{\"msg\":\"hi, this is cute server\"}";
                break;
            case "/time":
                Date now = new Date();
                response = "{\"msg\":\"hi, server time: " + sdf.format(now)+"\"}";
                break;
            case "/redirect301":
                isRedirect = true;
                status = HttpResponseStatus.MOVED_PERMANENTLY;
                redirectUrl = REDIRECT_URL;
                response = "\n";
                break;
            case "/redirect302":
                isRedirect = true;
                status = HttpResponseStatus.FOUND;
                redirectUrl = REDIRECT_URL;
                response = "\n";
                break;
            default:
                response = "{\"msg\":\"unknown uri " + uri + "\"}";
                break;
        }

        FullHttpResponse httpResponse;
        byte[] bytes = response.getBytes("UTF-8");
        if(!isRedirect) {
            httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(bytes));

            httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
            if (HttpUtil.isKeepAlive(request)) {
                httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.CONNECTION);
            }
        } else {
            httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(bytes));
            httpResponse.headers().set(HttpHeaderNames.LOCATION, redirectUrl)
                    .set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");;

        }
        ctx.channel().writeAndFlush(httpResponse);
    }
}
