package com.oneboxtech.se.webcache;

import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.model.IndexFileItem;
import com.oneboxtech.se.webcache.model.IndexFileUnordered;
import com.oneboxtech.se.webcache.util.CommonUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 5:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestDoc {

    @Test
    public void test() throws IOException {
        RandomAccessFile d = new RandomAccessFile(new File("/tmp/webcache/1/0.store"),"r");
        Document doc = Document.readOne(d,0);
        System.out.println(doc);
    }

    @Test
    public void testIndexFileUnordered() throws IOException {

        List<IndexFileItem> ls = IndexFileUnordered.readAllFromFile("/tmp/webcache/1/0.uidx");
        RandomAccessFile d = new RandomAccessFile(new File("/tmp/webcache/1/0.store"),"r");
        for (IndexFileItem l : ls) {
            System.out.println(l);
        }
        Collections.sort(ls,new Comparator<IndexFileItem>() {
            @Override
            public int compare(IndexFileItem o1, IndexFileItem o2) {
                return (int)(o1.getHash() - o2.getHash());
            }
        });

        for (IndexFileItem l : ls) {
            Document doc = Document.readOne(d,l.getOffset());
            System.out.println(doc);
        }

    }


    private Comparator<IndexFileItem> cmp = new Comparator<IndexFileItem>() {
        @Override
        public int compare(IndexFileItem o1, IndexFileItem o2) {
            return (int) (o1.getHash()- o2.getHash());
        }
    };

    @Test
    public void binarySearchTest(){
        ArrayList<IndexFileItem> arr = new ArrayList<IndexFileItem>();

        IndexFileItem i = new IndexFileItem();
        i.setHash(1406717258267L);
        arr.add(i);

         i = new IndexFileItem();
        i.setHash(1253095402317L);
        arr.add(i);

        i = new IndexFileItem();
        i.setHash(377401976069L);
        arr.add(i);

         i = new IndexFileItem();
        i.setHash(1966213590790L);
        arr.add(i);
//
//        arr.add(1406717258267L);
//        arr.add(1253095402317L);
//        arr.add(1966213590790L);
//        arr.add(377401976069L);
//        arr.add(1219354612945L);

        Collections.sort(arr);

        ArrayList<Long> test = new ArrayList();

        for (IndexFileItem item : arr) {
            test.add(item.getHash());
        }
        System.out.println("#####----"+ CommonUtil.join(test, ",") );;



//        for (Long aLong : arr) {
//            System.out.println(aLong);
//        }

//        int idx = Arrays.binarySearch(arr,1966213590790L);
    }

}
