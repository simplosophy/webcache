package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.nio.res.IODocument;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-1
 * Time: 下午3:50
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class WriteServerHandler extends SimpleChannelUpstreamHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BlockingDocumentStream stream;

    public WriteServerHandler(BlockingDocumentStream stream) {
        this.stream = stream;
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
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws java.lang.Exception {

        logger.debug("msg received.. msg class: {}",e.getMessage().getClass());
        System.out.println(e.getMessage());
        if(e.getMessage() instanceof IODocument){
            IODocument d = (IODocument) e.getMessage();
            if(d.getData() != null){

                logger.debug("received available doc: {}",d.toString());
                Document doc = new Document();
                doc.setMeta(d.getMeta());
                doc.setOrgUrlHash(d.getOrgUrlHash());
                doc.setData(d.getData());
                stream.getQueue().put(doc);
                if(doc.getOrgUrlHash() == -1){//end
                    logger.info("Received  Document with Hash==-1, please stop WriteServer");
                    e.getChannel().close();//close connection
                }
            }
        }
        ctx.sendUpstream(e);

    }
}
