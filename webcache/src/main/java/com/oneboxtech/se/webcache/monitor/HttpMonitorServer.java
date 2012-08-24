package com.oneboxtech.se.webcache.monitor;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-8
 * Time: 上午10:43
 * To change this template use File | Settings | File Templates.
 */
public class HttpMonitorServer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    int  port;
    private ServerBootstrap bootstrap;

    private MonitoringData data = new MonitoringData();
    public MonitoringData getMonitoringData(){
        return data;
    }

    public void run(){

        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipe = Channels.pipeline();
                pipe.addLast("decoder", new HttpRequestDecoder());
                pipe.addLast("encoder", new HttpResponseEncoder());
                pipe.addLast("handler", new HttpMonitorServerHandler(HttpMonitorServer.this));
                return pipe;
            }
        });

        bootstrap.bind(new InetSocketAddress(port));
        logger.info(String.format("HttpMonitorServer start listening port %d",port));
    }

    public HttpMonitorServer(int port){
        this.port = port;
    }

    public String toString(){
        return   String.format("HttpMonitorServer" +
                " port %d ",port);
    }

}
