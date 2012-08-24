package com.oneboxtech.se.webcache.util;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommonUtilTest {

    @Test
    public void test() throws URISyntaxException {
//        List<File> fs = CommonUtil.listRecursive(new File("/tmp/webcache"),"store");
//        for (File f : fs) {
//            System.out.println(f.getAbsolutePath());
//        }
        URI u = new URI("http://182.23.2.3:8080/237821?a=1&b=2&c&d=");
        System.out.println(u.getPath());
        System.out.println(u.getQuery());

        Map<String,String> m = CommonUtil.parsePara(u.getQuery());
        System.out.println(m);

    }

}
