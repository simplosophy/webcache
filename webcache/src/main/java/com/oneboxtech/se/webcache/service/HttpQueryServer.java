package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.index.IIndexReaderOpenedListener;
import com.oneboxtech.se.webcache.index.IndexReader;
import com.oneboxtech.se.webcache.model.BlackList;
import com.oneboxtech.se.webcache.model.BlackListFile;
import com.oneboxtech.se.webcache.monitor.HttpMonitorServer;
import com.oneboxtech.se.webcache.monitor.MonitoringData;
import com.oneboxtech.se.webcache.util.CommonUtil;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-4
 * Time: 上午11:12
 * To change this template use File | Settings | File Templates.
 */
public class HttpQueryServer implements IIndexReaderOpenedListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 监听的端口
     */
    int port;

    IndexReader[] readers;
    BlackList blackListDocSet;

    int openReaderCount = 0;

    private ServerBootstrap bootstrap;

    MonitoringData monitoringData;

    private String getPid() {
        String rtn = "";
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int idx = name.indexOf("@");
        if (idx > 0) {
            rtn = name.substring(0, idx);
        }
        return rtn;
    }


    public void run() {

        long startTime = System.currentTimeMillis();

        openReaderCount = 0;
        readers = new IndexReader[SystemConfig.FILE_STORE_DIRS.length];
        //start the index readers
        for (int i = 0; i < SystemConfig.FILE_STORE_DIRS.length; i++) {
            readers[i] = new IndexReader(SystemConfig.FILE_STORE_DIRS[i]);
            Thread thread = new Thread(readers[i]);
            readers[i].setListener(this);
            thread.start();
        }

        //wait until all readers are open
        while (openReaderCount < readers.length) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

         //load blacklisted docs
        logger.info("loading blacklist file"+SystemConfig.BLACKLIST_FILE);
        File file = new File(SystemConfig.BLACKLIST_FILE);
        if(!file.exists()){
            logger.warn("blacklist file does NOT exist, create a new one");
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                logger.error("blacklist file creation failed, Starting httpqueryserver without blacklist...");
                e.printStackTrace();
            }
        }
        try{
            BlackListFile blf = new BlackListFile(file);
            blackListDocSet = new BlackList(blf);
        }catch (IOException e){
            logger.error("loading blacklist file failed, Starting httpqueryserver without blacklist...");
            e.printStackTrace();
        }
        //end of load blacklisted docs

        //add shutdown handler
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                logger.info("Shutting down, write blacklist to file...");
                HttpQueryServer.this.blackListDocSet.dump();
            }
        });

        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipe = Channels.pipeline();
                pipe.addLast("decoder", new HttpRequestDecoder());
                pipe.addLast("encoder", new HttpResponseEncoder());
                pipe.addLast("handler", new HttpQueryServerHandler(HttpQueryServer.this));
                return pipe;
            }
        });

        //read pid file
        File pidFile = new File(SystemConfig.NODE_HTTPQUERYSERVER_PID);
        String previousHttpQueryServerPid = "";
        if (pidFile.exists()) {
            previousHttpQueryServerPid = CommonUtil.fileAsString(pidFile, "utf-8");
            if (!previousHttpQueryServerPid.equals("")) {
                logger.info("A Previous HttpQueryServer Maybe Running, try kill it...");
                String cmd = "kill " + previousHttpQueryServerPid;
                logger.info("Running Command: " + cmd);
                try {
                    Runtime.getRuntime().exec(cmd);
                    Thread.sleep(1000);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        //bootstrap.setOption("reuseAddress",true);//reuse port
        int tries = 5;
        for (; ; ) {
            try {
                tries--;
                bootstrap.bind(new InetSocketAddress(port));
                break;
            } catch (ChannelException e) {
                if (tries == 0) {
                    logger.error("Port {} may be using, HttpQueryServer starting failed", port);
                    return;
                }
                logger.error("Port {} may be using, try again..." + tries, port);
                try {
                    Thread.sleep((5 - tries) * 10000L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        //start monitor server
        logger.info("starting monitoring server");
        try {
            HttpMonitorServer monitorServer = new HttpMonitorServer(SystemConfig.MONITOR_PORT);
            monitorServer.run();
            monitoringData = monitorServer.getMonitoringData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //end of starting monitor server

        //start up success
        logger.info(String.format("HttpQueryServer start listening port %d", port));
        long endTime = System.currentTimeMillis();
        logger.info("HttpQueryServer Starting Time: {}ms", endTime - startTime);
        if (!pidFile.exists()) {
            pidFile.getParentFile().mkdirs();
            try {
                pidFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        try {
            String curPid = getPid();
            logger.info("Write Pid: {} to File" + pidFile.getCanonicalPath(), curPid);
            BufferedWriter writer = new BufferedWriter(new FileWriter(pidFile));
            writer.write(curPid);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public HttpQueryServer(int port) {
        this.port = port;
    }

    public String toString() {
        return String.format("HttpQueryServer" +
                " port %d ", port);
    }

    public static void main(String[] args) {
//        Integer port;
//        if(args.length < 1 ){
//            System.out.println("usage: java com.oneboxtech.se.webcache.service.HttpQueryServer  [baseDir] [true|false]");
//
//        }
//        port = CommonUtil.tryParseInt(args[0]);
//        if(port == null)
//            port =SystemConfig.NODE_HTTPQUERYSERVER_PORT;
        new HttpQueryServer(SystemConfig.NODE_HTTPQUERYSERVER_PORT).run();

    }

    @Override
    public synchronized void indexOpenFinished(IndexReader reader) {
        openReaderCount++;
        logger.info(String.format("[%d/%d] IndexReader of Dir: %s  Has Finished", openReaderCount, readers.length, reader.getBaseDir()));
    }
}
