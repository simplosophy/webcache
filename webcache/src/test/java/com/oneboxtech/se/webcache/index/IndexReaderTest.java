package com.oneboxtech.se.webcache.index;

import org.junit.Test;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexReaderTest {

    @Test
    public void test() throws IOException {
        IndexReader ir = new IndexReader("/tmp/webcache");
        ir.open();
        System.out.println(ir.query(4759297275166421545L));
        ir.close();
    }

}
