package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.nio.req.IOUrlHash;
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
 * Date: 31/7/12
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class ConsoleQueryClient {

    private int serverPort;
    private String serverHostname;

    public ChannelFuture getFuture() {
        return future;
    }

    private ChannelFuture future;
    private ClientBootstrap client;

    public ConsoleQueryClient(String serverHostname, int serverPort) {
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

            Long hash = CommonUtil.tryParseLong(line);
            if( hash == null){
                System.out.println("wrong long value.");
                continue;
            }

            System.out.println("querying : "+String.format("0X%X",hash));

            IOUrlHash h = new IOUrlHash(hash);

            // Sends the received line to the server.
            lastWriteFuture = channel.write(h);

            // If user typed the 'bye' command, wait until the server closes
            // the connection.
        }


        //future.getChannel();
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        ConsoleQueryClient c = new ConsoleQueryClient("127.0.0.1", 19527);
        c.run();
//        for (IOUrlHash i : getHash()) {
//
//            c.getFuture().getChannel().write(i);
//        }

//        c.getFuture()
    }

}
