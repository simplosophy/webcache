package com.oneboxtech.se.webcache.model;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 9:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexFileTest {

    @Test
    public void test() throws IOException {
        File  f = new File("/tmp/webcache/1/0.idx");
        IndexFile idf = new IndexFile(f);
        idf.openRead();
        InMemIndex mi = idf.toMemIndex();
        System.out.println(mi);
        idf.closeRead();

        DocumentFile docF = new DocumentFile(new File("/tmp/webcache/1/0.store"));

        docF.openRead();
        for (int i : mi.getOffsetArray()) {
            System.out.println(docF.readOne(i));

        }
        docF.closeRead();


    }

}
