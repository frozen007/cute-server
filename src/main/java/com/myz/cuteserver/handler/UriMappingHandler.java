package com.myz.cuteserver.handler;

import com.myz.cuteserver.processor.UriMappingProcessor;
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

/**
 * @author: zhaomingyumt
 * @date: 2023/11/24 7:23 PM
 * @description:
 */
@Deprecated
public class UriMappingHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(UriMappingHandler.class);

    private HttpRequest request;

    private UriMappingProcessor uriMappingProcessor;

    public UriMappingHandler(UriMappingProcessor uriMappingProcessor) {
        this.uriMappingProcessor = uriMappingProcessor;
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

        Object result = null;
        try {
            result = uriMappingProcessor.executeWithUri(uri);
        }catch (NoSuchMethodException e) {
            ctx.channel().writeAndFlush(returnWithStatus("uri is not found", HttpResponseStatus.NOT_FOUND));
            return;
        }
        catch (Exception e) {
            logger.error("exception when executeWithUri", e);
            ctx.channel().writeAndFlush(returnWithStatus("exception: " + e.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR));
            return;
        }

        String response = result == null ? "null" : result.toString();

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

    private FullHttpResponse returnWithStatus(String msg, HttpResponseStatus status) throws UnsupportedEncodingException {
        byte[] bytes = msg.getBytes("UTF-8");
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(bytes));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        return httpResponse;
    }
}
