package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.util.CommonUtil;
import com.oneboxtech.se.webcache.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 9:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class SystemConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SystemConfig.class);


    public static final  int NODE_CURRENT_INDEX;
//    @Deprecated
    //public static final  int NODE_CURRENT_PORT_INDEX;

    public static final  int NODE_COUNT;

    public static final  int NODE_WRITE_PORT;

    public static final  int MONITOR_PORT;
    public static final  int NODE_HTTPQUERYSERVER_PORT;
    public static final  String NODE_HTTPQUERYSERVER_PID;
    public static final int[] NODE_QUERY_PORTS;


//    @Deprecated
//    public static final  String STORE_DIR;

    public static final  int[] FILENAME_BASES;
    public static final String[] FILE_STORE_DIRS;

    public static final String DUMP_PROCESS_FILE;
    public static final String BLACKLIST_FILE;

    static {
        LOG.info("initializing system config...");
        NODE_CURRENT_INDEX = Integer.parseInt(PropertyUtil.getProperty("node.current.index","0"));
        //NODE_CURRENT_PORT_INDEX = Integer.parseInt(PropertyUtil.getProperty("node.current.port.index","0"));
        NODE_COUNT = Integer.parseInt(PropertyUtil.getProperty("node.count","5"));
        NODE_WRITE_PORT = Integer.parseInt(PropertyUtil.getProperty("node.write.port","9917"));
        NODE_HTTPQUERYSERVER_PORT = Integer.parseInt(PropertyUtil.getProperty("node.httpqueryserver.port","8080"));
        MONITOR_PORT= Integer.parseInt(PropertyUtil.getProperty("monitor.port","2601"));
        String s = PropertyUtil.getProperty("filename_bases","200,40");
        FILENAME_BASES = CommonUtil.tryParseIntArray(s,",");
        FILE_STORE_DIRS=PropertyUtil.getProperty("file_store_dirs","/tmp/data01,/tmp/data02,/tmp/data03,/tmp/data04").split(",");

//        STORE_DIR = PropertyUtil.getProperty("store_dir", "/usr/share/webcache");

        DUMP_PROCESS_FILE = PropertyUtil.getProperty("dump.processfile","/data01/webcache/dump.process");
        BLACKLIST_FILE = PropertyUtil.getProperty("file_blacklist","/data01/webcache/blacklist");
        NODE_HTTPQUERYSERVER_PID = PropertyUtil.getProperty("node.httpqueryserver.pid","/data01/webcache/httpqueryserver.pid");
        s = PropertyUtil.getProperty("node.query.ports", "9527,9528,9529,9530");
        NODE_QUERY_PORTS = CommonUtil.tryParseIntArray(s,",");


    }

}
