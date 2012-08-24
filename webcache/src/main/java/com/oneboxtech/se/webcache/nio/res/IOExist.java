package com.oneboxtech.se.webcache.nio.res;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 2/8/12
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class IOExist {
    private boolean exist;


    public IOExist() {
    }

    public IOExist(boolean exist) {
        this.exist = exist;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }
}
