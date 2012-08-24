package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.index.IndexReader;
import com.oneboxtech.se.webcache.util.CommonUtil;
import com.oneboxtech.se.webcache.util.FileNameHelper;
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class QueryServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

      int port;

     int portIndex;

    private String baseDir;

    private ServerSocketChannel channel;

     IndexReader[] readers;

    private File base;

    public QueryServer(int port, String baseDir)  {
        this.port = port;
        this.baseDir = baseDir;
    }

    private void init() throws Exception {
        base = new File(baseDir);
        if(!base.exists()) {
            throw  new Exception("baseDir不存在，创建空目录");
        }
        if(!base.isDirectory()){
            throw new Exception("baseDir应为目录");
        }
        File[] subDirs = base.listFiles(new  FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(dir.isDirectory())
                    return true;
                return false;
            }
        });
        //为每个子文件夹打开一个IndexReader
        if(subDirs != null){
            int readerNum = SystemConfig.FILENAME_BASES[0];//reader的数目应该等于一级目录数目
            readers = new IndexReader[readerNum];
            for(int i=0; i<readers.length;i++){
                String indexFileName = base.getAbsolutePath()  //+"/"+ SystemConfig.NODE_CURRENT_PORT_INDEX
                        + "/" +FileNameHelper.getNameSegment(i);
                logger.debug("indexFileName:{}",indexFileName);
                readers[i] = new IndexReader(indexFileName);
                try{
                    readers[i].open();
                }catch (IOException e){//index dir 可能不存在
                    logger.warn("IndexReader for dir: {} open failed, Possibly it doesn't exist.",indexFileName);
                }
            }

        }
        for (int i=0; i<SystemConfig.NODE_QUERY_PORTS.length; i++){
            if(SystemConfig.NODE_QUERY_PORTS[i] == this.port){
                this.portIndex = i;
                break;
            }
        }
    }

    public void run() throws Exception {
        logger.info("starting query server...");
        long start = System.currentTimeMillis();
        init();
        ServerBootstrap server = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()
        ));
        server.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {


                return Channels.pipeline(
//                        new ZlibEncoder(ZlibWrapper.GZIP),
//                        new ZlibDecoder(ZlibWrapper.GZIP),
//                        new StringDecoder(CharsetUtil.UTF_8),
//                        new StringEncoder(CharsetUtil.UTF_8),
                        new ObjectEncoder(),
                        new ObjectDecoder(new ClassResolver() {
                            @Override
                            public Class<?> resolve(String className) throws ClassNotFoundException {
                                return Class.forName(className);
                            }
                        }),
                        new QueryServerHandler(QueryServer.this)
                );
            }
        });
        server.bind(new InetSocketAddress(port));
        logger.info(String.format("Query Server start listening: %d with base_dir: %s",port, baseDir));

    }

    public String toString(){
        return String.format("QueryServer Instance listening: %d with base_dir: %s",port, baseDir);
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 2 ){
            System.out.println("usage: java com.oneboxtech.se.webcache.service.QueryServer [port] [baseDir]");
           return;
        }

        int port  = CommonUtil.tryParseInt(args[0]) ;
        new QueryServer(port,args[1]).run();

//        new QueryServer(19527,"/tmp/webcache/data01").run();
//        new QueryServer(19528,"/tmp/webcache/data02").run();
//        new QueryServer(19529,"/tmp/webcache/data03").run();
//        new QueryServer(19530,"/tmp/webcache/data04").run();

    }

}
