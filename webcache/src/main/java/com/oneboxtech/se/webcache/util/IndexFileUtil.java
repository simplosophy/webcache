package com.oneboxtech.se.webcache.util;

import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.model.IndexFileItem;
import com.oneboxtech.se.webcache.service.SystemConfig;
import org.eclipse.jdt.internal.core.SourceType;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-5
 * Time: 下午3:51
 * To change this template use File | Settings | File Templates.
 */
public class IndexFileUtil {

    public static List<IndexFileItem> readAllFromFile(String uidxFilePath) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(new File(uidxFilePath)));
        ArrayList<IndexFileItem> rtn = null;
        try{
            int version = in.readInt();
            int meta = in.readInt();
            System.out.println("version: "+version);
            System.out.println("meta: "+meta);;
            rtn = new ArrayList<IndexFileItem>();
            while (true){
                IndexFileItem item = new IndexFileItem();
                item.setHash(in.readLong());
                item.setOffset(in.readInt());
                BigInteger orgHash = new BigInteger(String.valueOf(item.getHash()));
                 orgHash = orgHash.multiply(new BigInteger(String.valueOf(SystemConfig.NODE_COUNT)));
                orgHash = orgHash.add(new BigInteger(String.valueOf(SystemConfig.NODE_CURRENT_INDEX)));
                System.out.println(String.format("Hash:%d offset:%d Origin_Hash:%d",item.getHash(),item.getOffset(),orgHash));
                rtn.add(item);
            }
        }catch (EOFException e){

        }
        return rtn;
    }

    public static void readFromStoreFile(String storeFile, IndexFileItem idx) throws IOException {
        RandomAccessFile in = new RandomAccessFile(new File((storeFile)),"rw");
        System.out.println("hash:"+idx.getHash());
        long orgHash = SystemConfig.NODE_COUNT*idx.getHash()+SystemConfig.NODE_CURRENT_INDEX;
        System.out.println("origin_hash:"+orgHash);
        Document doc = Document.readOne(in,idx.getOffset());
        byte [] newByte = Document.gzipUnCompress(doc.getData());
        String txt = new String(newByte,"utf-8") ;
        System.out.println(txt);
        //System.out.println(doc);
        in.close();
    }

    public static void main(String[] args) throws IOException {
//        List<IndexFileItem> ls = readAllFromFile("/home/shangrenxiang/wly/sandbox/shangrenxiang/webcache/0.uidx");
//        for (IndexFileItem l : ls) {
//            readFromStoreFile("/home/shangrenxiang/wly/sandbox/shangrenxiang/webcache/0.store",l);
//        }


        if(args.length < 1 ){
            System.out.println("usage: java com.oneboxtech.se.webcache.util.IndexFileUtil [index file path]");
            return;
        }
        readAllFromFile(args[0]) ;

    }


}
