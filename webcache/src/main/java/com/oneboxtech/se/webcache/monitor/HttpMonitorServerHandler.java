package com.oneboxtech.se.webcache.monitor;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-8
 * Time: 上午10:47
 * To change this template use File | Settings | File Templates.
 */
public class HttpMonitorServerHandler extends SimpleChannelUpstreamHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HttpMonitorServer server;

    public HttpMonitorServerHandler(HttpMonitorServer server) {
        this.server = server;
    }

    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (this == ctx.getPipeline().getLast()) {
            logger.warn(
                    "EXCEPTION, please implement " + getClass().getName() +
                    ".exceptionCaught() for proper handling.", e.getCause());
        }
        e.getChannel().close();
    }
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object resp = null;
         if (e.getMessage() instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) e.getMessage();

                HttpResponse res = null;
                //TODO
                URI uri = new URI(req.getUri());
            res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
             res.setContent(ChannelBuffers.copiedBuffer(server.getMonitoringData().toString(), Charset.forName("utf-8")));
             resp = res;
         }
        e.getChannel().write(resp).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channelFuture.getChannel().close();
            }
        });
    }


}
