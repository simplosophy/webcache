package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.index.IndexReader;
import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.nio.req.IOIfExist;
import com.oneboxtech.se.webcache.nio.res.IODocument;
import com.oneboxtech.se.webcache.nio.req.IOUrlHash;
import com.oneboxtech.se.webcache.nio.res.IOExist;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class QueryServerHandler extends SimpleChannelUpstreamHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private QueryServer server;

    private static int count = 0;

    @Override
    public void channelClosed(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        count--;
        logger.info("Client Closed... total count:" + count);
        ctx.sendUpstream(e);
    }

    @Override
    public void channelOpen(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        count++;
        logger.info("Client Connected... total count:" + count);
        ctx.sendUpstream(e);
    }

    public QueryServerHandler(QueryServer server) {
        this.server = server;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error(e.getCause().getMessage());
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws java.lang.Exception {

        Object o = e.getMessage();
        Object res = new Object();
        if (o != null && o instanceof IOUrlHash) {
            IODocument dd = new IODocument();
            IOUrlHash hash = (IOUrlHash) o;
            long orghash = hash.getHash();
            logger.debug( server+" received a query: {}", String.format("0X%X", orghash));
            long t = orghash & 0X7FFFFFFFFFFFFFFFL;
            if (t % SystemConfig.NODE_QUERY_PORTS.length == server.portIndex) {
                t /= SystemConfig.NODE_QUERY_PORTS.length;
                int readerIndex = (int) (t % server.readers.length);

                if (server.readers[readerIndex].isOpened()) {
                    Document d = server.readers[readerIndex].query(orghash);
                    logger.debug("write doc: " + d);
                    if (d != null) {
                        dd.setData(d.getData());
                        dd.setMeta(d.getMeta());
                        dd.setOrgUrlHash(d.getOrgUrlHash());
//                        future.addListener(Channe);
                    } else {

                        logger.warn(server+ ": query: {} has no record", String.format("0X%X", orghash));
                    }
                }

            } else {
                logger.warn(server+ ": query: {} has mapped to the wrong PORT", String.format("0X%X", orghash));
            }
            res = dd;
        } else if (o instanceof IOIfExist) { //查询是否存在
            IOExist exist = new IOExist();
            IOIfExist hash = (IOIfExist) o;
            long orghash = hash.getOrgHash();
            logger.debug(server+ ": received a if-exist query: {}", String.format("0X%X", orghash));
            long t = orghash & 0X7FFFFFFFFFFFFFFFL;
            if (t % SystemConfig.NODE_QUERY_PORTS.length == server.portIndex) {
                t /= SystemConfig.NODE_QUERY_PORTS.length;
                int readerIndex = (int) (t % server.readers.length);

                if (server.readers[readerIndex].isOpened()) {
                    boolean ee = server.readers[readerIndex].exists(orghash);
                    exist.setExist(ee);
                }

            } else {
                logger.warn(server+ ": query: {} has mapped to the wrong PORT", String.format("0X%X", orghash));
            }
            res = exist;
        }
        ChannelFuture future = e.getChannel().write(res); //write doc to client
//        future.setSuccess();
    }


}
