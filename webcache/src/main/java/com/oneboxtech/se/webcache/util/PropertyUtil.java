package com.oneboxtech.se.webcache.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 29/7/12
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */

public class PropertyUtil {

    private static Properties props = new Properties();
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);

    static{
        File configFile = new File("system.properties");
        InputStream is = null;
        if(configFile.exists()){
            logger.debug("使用配置文件:{}",configFile.getAbsolutePath());
            try {
                is = new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                logger.error("未找到配置文件{}！","system.properties");
                throw new RuntimeException();
            }
        }else{
            is = PropertyUtil.class.getClassLoader().getResourceAsStream("system.properties");
            logger.debug("使用classpath下的配置文件！");
        }
        if(is != null){
            try {
                props.load(is);
            } catch (IOException e) {
                logger.error("配置文件读取有误，请检查！");
                throw new RuntimeException(e);
            }finally {
                if(is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            }
            logger.debug("配置文件路径为{}",configFile.getAbsolutePath());

        }
    }

    public static String getProperty(String key,String defaultVal){
        return props.getProperty(key,defaultVal);
    }
}