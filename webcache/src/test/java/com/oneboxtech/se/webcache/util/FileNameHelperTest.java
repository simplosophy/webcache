package com.oneboxtech.se.webcache.util;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-7-29
 * Time: 下午6:33
 * To change this template use File | Settings | File Templates.
 */
public class FileNameHelperTest {

    @Test
    public void test() {
//        for (int i=0; i<1000; i++){
//            System.out.println(i+":"+ FileNameHelper.getNameSegment(i));
//        }
        long[] urlhash = new long[]{0x121*5, 0xF02*5,0XE03*5};
        for(int i=0; i<100;i++){


//            System.out.println(String.format("%x",i)+":"+ FileNameHelper.getFileFullName(i*5,"idx"));
            System.out.println(FileNameHelper.getFileFullName(i*5,"test"));
        }

    }

}
