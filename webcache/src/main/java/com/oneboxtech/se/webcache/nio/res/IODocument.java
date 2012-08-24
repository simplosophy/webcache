package com.oneboxtech.se.webcache.nio.res;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */

import com.oneboxtech.se.webcache.service.SystemConfig;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * 通信的对象
 */
public class IODocument implements Serializable {

    private long orgUrlHash;

    private int meta = 0;

    private byte[] data;

    public long getOrgUrlHash() {
        return orgUrlHash;
    }

    public void setOrgUrlHash(long orgUrlHash) {
        this.orgUrlHash = orgUrlHash;
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
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

            if(data == null)return null;

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
