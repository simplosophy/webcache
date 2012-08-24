package com.oneboxtech.se.webcache.index;

import com.oneboxtech.se.webcache.DocumentStreamHelper;
import com.oneboxtech.se.webcache.exception.BuildIndexException;
import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.model.proto.WebCacheDocProtos;
import com.oneboxtech.se.webcache.service.BlockingDocumentStream;
import com.oneboxtech.se.webcache.util.CommonUtil;
import com.oneboxtech.se.webcache.util.FileNameHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BinaryComparable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class IndexBuilderTest {

    @Test
    public void testBuild() throws IOException, BuildIndexException {
        IndexBuilder ib = new IndexBuilder();
        DocumentStreamHelper docS = new DocumentStreamHelper(1000);
        ib.build(docS);
        for (long l : docS.getGeneratedHash()) {
            System.out.println(l);
        }

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

    @Test
    public void testBuildFromSeqFile() throws IOException, InterruptedException {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.getLocal(conf);
        String file_input = "/tmp/webcache/block_004-00000";
        SequenceFile.Reader reader = null;
        IndexBuilderRunnable builder = new IndexBuilderRunnable(new BlockingDocumentStream());
        Thread thread = new Thread (builder);
        thread.start();
        long startTime = System.currentTimeMillis();
        ArrayList<Long> storedHash = new ArrayList<Long>();
        try {
            reader = new SequenceFile.Reader(fs, new Path(file_input), conf);
            String keyclass = reader.getKeyClassName();
            String valueclass = reader.getValueClassName();
            Writable key_writable =
                    (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
            Writable value_writable =
                    (Writable)ReflectionUtils.newInstance(reader.getValueClass(), conf);
            while (reader.next(key_writable, value_writable)) {
                byte[] k = getBytes(key_writable),v = getBytes(value_writable);
                String key = new String(k,"utf-8");
                int i = key.indexOf('_');
                Long hash = CommonUtil.tryParseLong(key.substring(0, i));
                if(hash != null){
                   Document doc = new Document();
                   doc.setOrgUrlHash(hash);
                   doc.setData(v);
                    WebCacheDocProtos.WebCacheDoc pd = WebCacheDocProtos.WebCacheDoc.parseFrom(v);
                    String page = new String(Document.gzipUnCompress(pd.getGzipCompressedUtf8Page().toByteArray()),"utf-8");
                    String title= new String((pd.getTitle().toByteArray()),"utf-8");
                    Timestamp t = new Timestamp(pd.getCrawlTime()/1000);

                    System.out.println(page);
                    System.out.println(title);
                    System.out.println(t);
                        //builder.getDocQueue().getQueue().put(doc);
                        //storedHash.add(hash) ;
                }
//                System.out.println(new String(k,"utf-8"));
//                System.out.println(new String(v,"utf-8"));
            }
            Document doc = new Document();
            doc.setOrgUrlHash(-1);
            builder.getDocQueue().getQueue().put(doc);//send end

            long endTime = System.currentTimeMillis();


            for (Long l : storedHash) {
                System.out.println(l);
            }
            System.out.println(storedHash.size()+" documents");
            System.out.println("TimeSpan: "+(endTime - startTime));
            while (thread.isAlive()){
                Thread.sleep(1000);
            }
        } finally {
            if(reader != null)
                IOUtils.closeStream(reader);
        }


    }

}
