package com.oneboxtech.se.webcache.nio.req;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class IOUrlHash implements Serializable {


    private long hash;

    public IOUrlHash() {
    }

    public IOUrlHash(long hash) {

        this.hash = hash;
    }

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }
}
