package com.oneboxtech.se.webcache.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-18
 * Time: 下午4:03
 * To change this template use File | Settings | File Templates.
 */
public class BlackList {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Set<Long> blackDocs;
    private BlackListFile file;
    private DumpTread dumpTask;
    private Timer timer;

    private static final long TIMER_DELAY= 20*1000L;//start blacklist service after 20 seconds
    private static final long TIMER_PERIOD= 10*60*1000L;//dump blacklists to file every 10 minutes

    public BlackList(BlackListFile file) throws IOException {
        this.file = file;
        blackDocs = Collections.synchronizedSet( new HashSet<Long>(20000));
            file.openRead();
            try{
            for(;;){
                long  l = file.readOne();
                blackDocs.add(l);
            }
            }catch (EOFException eof){
            }
            file.closeRead();

        dumpTask = new DumpTread();
        timer = new Timer("BlackList Timer");
        timer.schedule(dumpTask, TIMER_DELAY, TIMER_PERIOD);
    }

    /**
     * dump now
     */
    public void dump(){
        dumpTask.run();
    }

    public void clear(){
        blackDocs.clear();
    }

    /**
     * STOP dumping task
     */
    public void stopDumpTask(){
        dumpTask.cancel();
        timer.cancel();
    }

    public boolean exist(long docHash){
       return blackDocs.contains(docHash);
    }

    public void add(long docHash){
        blackDocs.add(docHash);
    }

    public void remove(long  docHash){
        blackDocs.remove(docHash);
    }

    private class DumpTread extends TimerTask {

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                BlackList.this.file.openWrite();
                synchronized (BlackList.this.blackDocs){
               	    Iterator i = BlackList.this.blackDocs.iterator();
                    while (i.hasNext()){
                        Object o = i.next();
                        if(o != null)
                            BlackList.this.file.writeOne((Long) o);
                    }
                }
                BlackList.this.file.closeWrite();
                long  end = System.currentTimeMillis();
                logger.info("Dumping blacklist finished, it takes {}ms",end-start);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

}
