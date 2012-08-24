package com.oneboxtech.se.webcache.nio.req;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 2/8/12
 * Time: 5:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class IOIfExist {
    private long orgHash;

    public long getOrgHash() {
        return orgHash;
    }

    public void setOrgHash(long orgHash) {
        this.orgHash = orgHash;
    }

    public IOIfExist() {

    }

    public IOIfExist(long orgHash) {

        this.orgHash = orgHash;
    }
}
