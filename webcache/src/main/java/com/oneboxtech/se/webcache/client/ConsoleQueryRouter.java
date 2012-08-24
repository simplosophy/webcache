package com.oneboxtech.se.webcache.client;

import com.oneboxtech.se.webcache.nio.req.IOUrlHash;
import com.oneboxtech.se.webcache.service.SystemConfig;
import com.oneboxtech.se.webcache.util.CommonUtil;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolver;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 2/8/12
 * Time: 8:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleQueryRouter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ClientBootstrap[] clients;
    private ChannelFuture[] futures;

    private String queryServerHostName;

    /**
     *
     *直接连接到QueryServer上面的所有端口上
     * @param queryServerHostName QueryServer节点名称
     */
    public ConsoleQueryRouter(String queryServerHostName) {
        this.queryServerHostName = queryServerHostName;
    }

    public void run(){
        clients = new ClientBootstrap[SystemConfig.NODE_QUERY_PORTS.length];
        futures = new ChannelFuture[SystemConfig.NODE_QUERY_PORTS.length];
        for(int i=0; i<SystemConfig.NODE_QUERY_PORTS.length;i++){
            clients[i] = new ClientBootstrap(new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()
            ));
            clients[i].setPipelineFactory(new ChannelPipelineFactory() {
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
                            new ConsoleQueryRouterHandler(ConsoleQueryRouter.this)
                    );
                }
            });
            futures[i] = clients[i].connect(new InetSocketAddress(queryServerHostName, SystemConfig.NODE_QUERY_PORTS[i]));

            // Wait until the connection attempt succeeds or fails.
            Channel channel = futures[i].awaitUninterruptibly().getChannel();
            if (!futures[i].isSuccess()) {
                futures[i].getCause().printStackTrace();
                clients[i].releaseExternalResources();
                continue;
            }
            logger.info("QueryRouter connected to "+ queryServerHostName+":"+SystemConfig.NODE_QUERY_PORTS[i]);
        }

    }

    //TODO sync
    public void query(long orgHash){
        long t = orgHash;
        t &= 0X7FFFFFFFFFFFFFFFL;
        t /= SystemConfig.NODE_COUNT;
        int idx = (int)(t%SystemConfig.NODE_QUERY_PORTS.length);
        if(futures[idx] == null){
            logger.warn("Connection to "+queryServerHostName+":" + SystemConfig.NODE_QUERY_PORTS[idx] + " is down");
            return;
        }
        IOUrlHash q = new IOUrlHash(orgHash);
        futures[idx].getChannel().write(q);

    }


    public static void main(String[] args) throws InterruptedException, IOException {

        if(args.length<1){
            System.out.println("usage: java com.oneboxtech.se.webcache.service.client.ConsoleQueryRouter [hostname]");
        }

        QueryRouter router = new QueryRouter(args[0]);
        router.run();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        for (; ; ) {
            String line = in.readLine();
            if (line == null) {
                break;
            }


            Long hash = CommonUtil.tryParseLong(line);
            if( hash == null){
                System.out.println("wrong long value.");
                continue;
            }

            System.out.println("querying : "+String.format("0X%X",hash));
            router.query(hash);
        }

    }

}
