package com.oneboxtech.se.webcache.client;

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 2/8/12
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleQueryRouterHandler extends SimpleChannelUpstreamHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private  ConsoleQueryRouter router;

    public ConsoleQueryRouterHandler(ConsoleQueryRouter router) {
        this.router = router;
    }

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
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        logger.debug("msg received.. msg class: {}",e.getMessage().getClass());
        // logger.debug(e.getMessage().toString());
        System.out.println(e.getMessage());
        ctx.sendUpstream(e);

    }
}
