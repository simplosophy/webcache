package com.oneboxtech.se.webcache.exception;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildIndexException extends Exception {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public BuildIndexException(String message) {
        super(message);
    }
}
