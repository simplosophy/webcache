package com.oneboxtech.se.webcache.service;

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 8:26 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class ConsoleQueryClientHandler extends SimpleChannelUpstreamHandler{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void channelClosed(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("Channel Closed by peer");
        ctx.sendUpstream(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error(e.getCause().getMessage());
        e.getCause().printStackTrace();
        //e.getChannel().close();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws java.lang.Exception {

        logger.debug("msg received.. msg class: {}",e.getMessage().getClass());
        System.out.println(e.getMessage());

        ctx.sendUpstream(e);

    }
}