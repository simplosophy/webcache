package com.oneboxtech.se.webcache.model;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IDocumentStream {

    Document read() throws InterruptedException;

}
