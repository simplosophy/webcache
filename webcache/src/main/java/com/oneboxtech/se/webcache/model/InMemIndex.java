package com.oneboxtech.se.webcache.model;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 9:33 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * 在内存中的索引文件
 */
public class InMemIndex {

    private int meta;
    private int version;

    private long[] hashArray;
    private int[] offsetArray;

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long[] getHashArray() {
        return hashArray;
    }

    public void setHashArray(long[] hashArray) {
        this.hashArray = hashArray;
    }

    public int[] getOffsetArray() {
        return offsetArray;
    }

    public void setOffsetArray(int[] offsetArray) {
        this.offsetArray = offsetArray;
    }

    @Override
    public String toString() {
        return "InMemIndex{" +
                "meta=" + meta +
                ", version=" + version +
                ", hashArray=" +  hashArray +
                ", offsetArray=" + offsetArray +
                '}';
    }
}
