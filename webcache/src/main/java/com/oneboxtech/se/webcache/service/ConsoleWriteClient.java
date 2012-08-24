package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.nio.res.IODocument;
import com.oneboxtech.se.webcache.util.CommonUtil;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolver;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 1/8/12
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class ConsoleWriteClient {
    private int serverPort;
    private String serverHostname;

    public ChannelFuture getFuture() {
        return future;
    }

    private ChannelFuture future;
    private ClientBootstrap client;

    public ConsoleWriteClient(String serverHostname, int serverPort) {
        this.serverPort = serverPort;
        this.serverHostname = serverHostname;
    }

    public void run() throws IOException {
        client = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()
        ));
        client.setPipelineFactory(new ChannelPipelineFactory() {
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
                        new ConsoleQueryClientHandler()
                );
            }
        });
        future = client.connect(new InetSocketAddress(serverHostname, serverPort));

        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.awaitUninterruptibly().getChannel();
        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            client.releaseExternalResources();
            return;
        }

        ChannelFuture lastWriteFuture = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        for (; ; ) {
            String line = in.readLine();
            if (line == null) {
                break;
            }
            if (line.toLowerCase().equals("bye")) {
//                channel.getCloseFuture().awaitUninterruptibly();
                channel.close();
                client.releaseExternalResources();
                break;
            }

            String[]  strings = line.split(",");

            try{
                Long h = CommonUtil.tryParseLong(strings[0]);
                Integer m = CommonUtil.tryParseInt(strings[1]);
                byte [] data = strings[2].getBytes("utf-8");
                IODocument doc = new IODocument();
                doc.setData(data);
                doc.setMeta(m);
                doc.setOrgUrlHash(h);
                System.out.println("writing : "+doc);

                lastWriteFuture = channel.write(doc);
            }catch (Exception e ){
                e.printStackTrace();
                System.out.println("wrong long value.");
                continue;
            }


        }

    }


    public static void main(String[] args) throws InterruptedException, IOException {
        ConsoleWriteClient c = new ConsoleWriteClient("127.0.0.1", 9917);
        c.run();
    }

}
