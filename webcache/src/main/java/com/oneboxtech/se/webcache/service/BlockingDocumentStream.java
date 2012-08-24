package com.oneboxtech.se.webcache.service;

import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.model.IDocumentStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-1
 * Time: 下午4:01
 * To change this template use File | Settings | File Templates.
 */
public class BlockingDocumentStream implements IDocumentStream {

    private BlockingQueue<Document> queue = new LinkedBlockingDeque<Document>(1024); //avoid heap overflow


    public BlockingQueue<Document> getQueue() {
        return queue;
    }

    @Override
    public Document read() throws InterruptedException {
            return queue.take();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
