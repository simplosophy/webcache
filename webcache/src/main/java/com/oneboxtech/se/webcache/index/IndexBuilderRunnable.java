package com.oneboxtech.se.webcache.index;

import com.oneboxtech.se.webcache.exception.BuildIndexException;
import com.oneboxtech.se.webcache.service.BlockingDocumentStream;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-1
 * Time: 下午8:04
 * To change this template use File | Settings | File Templates.
 */
public class IndexBuilderRunnable extends IndexBuilder implements Runnable {


    private BlockingDocumentStream docQueue;

    private IBuildCompleteListener listener;

    public IBuildCompleteListener getListener() {
        return listener;
    }

    public void setListener(IBuildCompleteListener listener) {
        this.listener = listener;
    }

    public BlockingDocumentStream getDocQueue() {
        return docQueue;
    }

    public IndexBuilderRunnable(BlockingDocumentStream docQueue) {
        this.docQueue = docQueue;
    }


    @Override
    public void run() {
        try {
            super.build(docQueue);
            if(listener != null){          //notify build listener
                listener.buildComplete(this);
            }
        } catch (BuildIndexException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
