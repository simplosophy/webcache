package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.exception.BuildIndexException;
import com.oneboxtech.se.webcache.index.IndexBuilder;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolver;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 1/8/12
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class WriteServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private  int port;

    private BlockingDocumentStream documentStream;

    private IndexBuilder builder;

    public WriteServer(int port) {
        this.port = port;
    }

    public void run(){
        ServerBootstrap server = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()
        ));
        documentStream = new BlockingDocumentStream();
        builder = new IndexBuilder();

        server.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {


                return Channels.pipeline(
                        new ObjectEncoder(),
                        new ObjectDecoder(new ClassResolver() {
                            @Override
                            public Class<?> resolve(String className) throws ClassNotFoundException {
                                return Class.forName(className);
                            }
                        }),
                        new WriteServerHandler(documentStream)
                );
            }
        });
        server.bind(new InetSocketAddress(port));
        logger.info("Write Server start listening {}",port);

        try {
            builder.build(documentStream);
        } catch (BuildIndexException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void main(String[] args) throws Exception {
//        if(args.length < 1 ){
//            System.out.println("usage: java com.oneboxtech.se.WriteServer.Main [port]");
//            return;
//        }

//        int port  = CommonUtil.tryParseInt(args[0]) ;
        new WriteServer(SystemConfig.NODE_WRITE_PORT).run();
    }

}
