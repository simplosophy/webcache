package com.oneboxtech.se.webcache.model;

import com.oneboxtech.se.webcache.service.SystemConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
 /**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 9:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class Document {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int META_GZIPED = 0;
    public static final int META_PROTO_BUF = 1;
    //public static final int META_COMPRESS_LZO = 0x2;

//    public static final int BUFF_SIZE = 4096;


    private long orgUrlHash;

    //private int meta = META_GZIPED;//version 0, gzip is default
    private int meta = META_PROTO_BUF;//version 1, use proto_buf version=1

    private byte[] data;

    //meta + orgUrlhash + data
    public int getRealLength(){
        return data.length+4+8+4;
    }


    /**
     * 将一个文档写入outputStream
     * @param output
     * @return 写入的长度，包含4个字节的meta信息
     * @throws IOException
     */
    public void writeOne(RandomAccessFile output) throws IOException {
        output.writeInt(meta);
        output.writeLong(orgUrlHash);
        output.writeInt(data.length);

//        if( (meta & META_COMPRESS_GZIP) != 0 ){
//            data = gzipCompress(this.data);
//        }

//        output.write(lzoCompress(data));
//        output.write(gzipCompress(data));
        output.write((data));
//        logger.debug("写入meta:{}", String.format("%X", meta));
//        logger.debug("写入url_hash:{}", String.format("%X", orgUrlHash));
//        logger.debug("写入data.length:{}", String.valueOf(data.length));
    }

    public String dataToString(String encoding) {
        try {
            return new String(data, encoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     *
     * @param input
     * @param offset offset in .store file
     * @return
     */
    public static Document readOne(RandomAccessFile input, int offset) throws IOException {
        Document rtn =null;
        input.seek(offset);

        rtn = new Document();
        int meta = input.readInt();
        rtn.setMeta(meta);
        rtn.setOrgUrlHash(input.readLong());
        int length = input.readInt();
        byte[] data = new byte[length];
        input.read(data);

//        if( (meta & META_COMPRESS_GZIP) != 0 ){
//            data = gzipUnCompress(data);
//        }

//        rtn.setData(lzoUncompress(data));
//        rtn.setData(gzipUnCompress(data));
        rtn.setData((data));
        input.seek(0);//回到文件头
        return rtn;
    }


    public static byte[] gzipUnCompress(byte[] input) throws IOException {
        GZIPInputStream ins = new GZIPInputStream( new ByteArrayInputStream(input) );
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        //GZIPOutputStream gout = new GZIPOutputStream(ous);
        byte []  buf = new byte[1024];
        int count;
        while ((count = ins.read(buf, 0,buf.length)) != -1){
            ous.write(buf,0,count);
        }
        return ous.toByteArray();
    }
    public static byte[] gzipCompress(byte[] input) throws IOException {
        DataInputStream ins = new DataInputStream( new ByteArrayInputStream(input) );
        ByteArrayOutputStream ous = new ByteArrayOutputStream(10*1024);
        GZIPOutputStream gout = new GZIPOutputStream(ous);
        byte []  buf = new byte[10240];
        int count;
        while ((count = ins.read(buf, 0,buf.length)) != -1){
            gout.write(buf,0,count);
        }
        gout.finish();
        gout.flush();
        return ous.toByteArray();
    }

    public static byte[] lzoCompress(byte[] input) throws IOException {
        CompressionCodec lzoCodec = null;
        try {
            Class<?> externalCodec = ClassLoader.getSystemClassLoader()
                    .loadClass("com.hadoop.compression.lzo.LzoCodec");
            lzoCodec = (CompressionCodec) ReflectionUtils.newInstance(
                    externalCodec, new Configuration());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        CompressionOutputStream outputStream = lzoCodec.createOutputStream(ous);

        outputStream.write(input);
        outputStream.finish();
        outputStream.flush();
        return ous.toByteArray();
    }


    public static byte[] lzoUncompress(byte[] input) throws IOException {
        CompressionCodec lzoCodec = null;
        try {
            Class<?> externalCodec = ClassLoader.getSystemClassLoader()
                    .loadClass("com.hadoop.compression.lzo.LzoCodec");
            lzoCodec = (CompressionCodec) ReflectionUtils.newInstance(
                    externalCodec, new Configuration());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        CompressionInputStream inputStream = lzoCodec.createInputStream(new ByteArrayInputStream(input));
        byte[] buffer = new byte[4096];

        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        while (true) {
            int bytesRead = inputStream.read(buffer);
            if (bytesRead < 0) {
                break;
            }
            ous.write(buffer, 0, bytesRead);
        }
        return ous.toByteArray();
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }


    public long getOrgUrlHash() {
        return orgUrlHash;
    }

    /**
     * NOTE:  请让orgUrlHash值大于0，即最高位为0，以防bug
     * 因为JAVA没有usigned类型，long的除法会保留原long的正负
     * @param orgUrlHash
     */
    public void setOrgUrlHash(long orgUrlHash) {
        this.orgUrlHash = orgUrlHash;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        try {

            String dataS =  new String(data, "utf-8");
            if(dataS.length() > 100){
                dataS = dataS.substring(0,100)+"......";
            }

            return "Document{" +
                    "orgUrlHash=" + String.format("OX%X", orgUrlHash) +
                    ", meta=" + String.format("%X", meta) +
                    ", data=" + dataS +
                    ", data.length=" + data.length +
                    '}';
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
