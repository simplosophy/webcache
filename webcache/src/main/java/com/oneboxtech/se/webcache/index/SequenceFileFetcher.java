package com.oneboxtech.se.webcache.index;

import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.service.SystemConfig;
import com.oneboxtech.se.webcache.util.CommonUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BinaryComparable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-4
 * Time: 下午2:53
 * To change this template use File | Settings | File Templates.
 */
public class SequenceFileFetcher implements Runnable {

    IndexBuilderRunnable[] builders;

    IFileReadCompleteListener listener;

    public void setListener(IFileReadCompleteListener listener){
        this.listener = listener;
    }

    List<Path> filesToRead;
    FileSystem fs;

    Configuration conf;

    public List<Path> getFilesToRead(){
        return filesToRead;
    }

    public SequenceFileFetcher(IndexBuilderRunnable[] builders, FileSystem fs, List<Path> filesToRead, Configuration conf) {
        this.builders = builders;
        this.fs = fs;
        this.conf = conf;
        this.filesToRead = filesToRead;
    }

    private static byte[] getBytes(Object o) throws IOException {
        if (o instanceof BinaryComparable) {
            BinaryComparable bo = (BinaryComparable) o;
            int len = bo.getLength();
            byte[] result = new byte[len];
            System.arraycopy(bo.getBytes(), 0, result, 0, len);
            return result;
        } else {
            return o.toString().getBytes("utf-8");
        }
    }

    @Override
    public void run() {

        for (Path path : filesToRead) {


            SequenceFile.Reader reader = null;
            try {

                reader = new SequenceFile.Reader(fs, path, conf);
                Writable key_writable =
                        (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
                Writable value_writable =
                        (Writable) ReflectionUtils.newInstance(reader.getValueClass(), conf);
                while (reader.next(key_writable, value_writable)) {
                    byte[] k = getBytes(key_writable), v = getBytes(value_writable);
                    String key = new String(k, "utf-8");
                    int i = key.indexOf('_');
                    Long hash = CommonUtil.tryParseLong(key.substring(0, i));
                    if (hash != null) {
                        if(hash < 0){ //make sure hash is none-negative
                            System.err.println("Received An Invalid Hash: " + hash);
                            continue;
                        }
                        Document doc = new Document();
                        doc.setOrgUrlHash(hash);
                        doc.setData(v);
                        int idx = (int) (hash% SystemConfig.FILE_STORE_DIRS.length);
                        BlockingQueue<Document> q = builders[idx].getDocQueue().getQueue();
                        q.put(doc); //route docs to consumers
                    } else {
                        System.err.println("Wrong long value: " + key.substring(0, i) + " it may be overflow");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (reader != null)
                    IOUtils.closeStream(reader);
            }


        }
        if(listener != null)            //notify listeners
            listener.fileReadComplete(this);
    }
}
