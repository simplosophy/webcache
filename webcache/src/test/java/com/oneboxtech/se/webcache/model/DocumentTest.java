package com.oneboxtech.se.webcache.model;

import com.oneboxtech.se.webcache.util.CommonUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-5
 * Time: 下午5:10
 * To change this template use File | Settings | File Templates.
 */
public class DocumentTest {

    @Test
    public void test() throws IOException {

        String s = CommonUtil.fileAsString(new File("/home/shangrenxiang/index.html"),"gbk");
        byte[] sBytes = s.getBytes("utf-8");

        System.out.println("orgin length: "+ sBytes.length);
        byte[] compressed = Document.lzoCompress(sBytes);
        System.out.println("compressed length: "+ compressed.length);

        byte[] uncompressed = Document.lzoUncompress(compressed);
        System.out.println("uncompressed length: "+ uncompressed.length);

        System.out.println("new string:"+ new String(uncompressed,"utf-8"));



    }

}
