package com.oneboxtech.se.webcache.util;

import com.oneboxtech.se.webcache.service.SystemConfig;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 11:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class TellWhere {

    @Test
    public void tell(){

        System.out.println(String.format("%3d", SystemConfig.NODE_CURRENT_INDEX).replace(' ','0'));

        long  k = 468548896808900L;
        k &= 0X7FFFFFFFFFFFFFFFL;
        //System.out.println(String.format("Hash Remain is: OX%X", FileNameHelper.getDocHashRemain(k)));
        System.out.println(String.format("file stored in:%s", FileNameHelper.getFileFullName(k,"store")));
        System.out.println(String.format("node index is: %d", k% SystemConfig.NODE_COUNT));
        k /= SystemConfig.NODE_COUNT;
        int portIdx  = (int)(k% SystemConfig.NODE_QUERY_PORTS.length);
        System.out.println(String.format("port index is: %d and it's : %d", portIdx, SystemConfig.NODE_QUERY_PORTS[portIdx]));
        k /= SystemConfig.NODE_QUERY_PORTS.length;
        int indexCount = SystemConfig.FILENAME_BASES[1];
        int indexReaderIdx = (int)(k%indexCount);
        System.out.println(String.format("IndexReader instance index is: %d", indexReaderIdx));

    }

}
