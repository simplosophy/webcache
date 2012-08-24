package com.oneboxtech.se.webcache;

import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.model.IDocumentStream;
import com.oneboxtech.se.webcache.service.SystemConfig;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentStreamHelper implements IDocumentStream {

    private List<Document> docs;

    static final char[] ALPHABET = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    private int count = 0,num = 100;

    public long[] getGeneratedHash() {
        return generatedHash;
    }

    private long[] generatedHash;




    public DocumentStreamHelper(  )  {
//        docs = new ArrayList<Document>();
//        String[] ss = new String[]{"html文档","html文档2","aaa this is a doc","traceroute my heart"};
//        long[] urlhash = new long[]{ 0X241*5,0x101*5, 0xF01*5,0XE03*5};
//        for(int i = 0 ; i<ss.length; i++){
//            Document d = new Document();
//            d.setOrgUrlHash(urlhash[i]);
//            String data = ss[i];
//            d.setData(data.getBytes("utf-8"));
//            docs.add(d);
//        }
        generatedHash =  new long[num];
    }

    public DocumentStreamHelper(int num) {
        this.num = num;
        generatedHash =  new long[num];
    }

    private Random random = new Random();

    private Document generateOne() throws UnsupportedEncodingException {
        Document d = new Document();
        d.setMeta(0);
        long hash = random.nextLong();
        hash &= 0XFFFFFFFFFFFFL;
//        hash *= SystemConfig.NODE_QUERY_PORTS.length;
//        hash += SystemConfig.NODE_CURRENT_PORT_INDEX;
        //hash *= SystemConfig.NODE_COUNT;
        //hash &= 0X7FFFFFFFFFFFFFFFL;
        hash += SystemConfig.NODE_CURRENT_INDEX;
        hash &= 0X7FFFFFFFFFFFFFFFL;

        d.setOrgUrlHash(hash);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<200; i++){
            int r = random.nextInt();
            r &= 0X7FFFFFFF;
            sb.append(ALPHABET[r%ALPHABET.length]);
        }
        d.setData(sb.toString().getBytes("utf-8"));
        System.out.println(d);
        return d;
    }

    @Override
    public Document read() {
//        if(count == docs.size())
//            return null;
//        Document rtn = docs.get(count);
//        count++;
//        return rtn;
        if(count == num)
             return null;
        Document rtn = null;
        try {
            rtn = generateOne();
            generatedHash[count] = rtn.getOrgUrlHash();
            count++;
        } catch (UnsupportedEncodingException e) {

        }
        return rtn;
    }

    @Test
    public void test() throws UnsupportedEncodingException, InterruptedException {
        IDocumentStream ds = new DocumentStreamHelper();
        Document d;
        while ((d = ds.read()) != null){
            System.out.println(d.getOrgUrlHash()+"\n"+d.getData());
        }
    }
}
