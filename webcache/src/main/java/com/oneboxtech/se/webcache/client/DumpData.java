package com.oneboxtech.se.webcache.client;

import com.oneboxtech.se.webcache.index.IBuildCompleteListener;
import com.oneboxtech.se.webcache.index.IFileReadCompleteListener;
import com.oneboxtech.se.webcache.index.IndexBuilderRunnable;
import com.oneboxtech.se.webcache.index.SequenceFileFetcher;
import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.service.BlockingDocumentStream;
import com.oneboxtech.se.webcache.service.SystemConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 2/8/12
 * Time: 5:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class DumpData implements IBuildCompleteListener, IFileReadCompleteListener {

    private String inputFile;

    public static int FETCH_THREAD = 8;
    public static int WRITE_THREAD = SystemConfig.FILE_STORE_DIRS.length;


    private HashSet<IndexBuilderRunnable> threadSet = new HashSet<IndexBuilderRunnable>();
    private HashSet<SequenceFileFetcher> producerThreadSet = new HashSet<SequenceFileFetcher>();

    private HashSet<String> alreadyReadFiles = new HashSet<String>();
    private BufferedWriter writerToAlreadyReadFiles;

    public DumpData(String inputFile) {
        this.inputFile = inputFile;
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.out.println("usage: java com.oneboxtech.se.webcache.service.client.DumpData [hdfs_file_path]");
        }
        new DumpData(args[0]).run();
//        new DumpData("/tmp/shangrenxiang/input_test.sf").run();
    }

    public void run() throws IOException, InterruptedException {

        File dumpF = new File(SystemConfig.DUMP_PROCESS_FILE);
        //read already read files
        try {
            if (dumpF.exists()) {
                BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(dumpF)));
                String l = null;
                while ((l = r.readLine()) != null) {
                    alreadyReadFiles.add(l);
                }
                r.close();
            }else {
                dumpF.getParentFile().mkdirs();
                dumpF.createNewFile();
            }
            writerToAlreadyReadFiles = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpF, true)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        String file_input = inputFile;


        long startTime = System.currentTimeMillis();
        Path baseDir = new Path(file_input);
        FileStatus[] files = fs.listStatus(baseDir, new PathFilter() {//列举出属于该节点的文件
            @Override
            public boolean accept(Path path) {
                String fName = path.getName();
                int idx = fName.indexOf('/');
                if (idx < 0) idx = 0;
                String realName = fName.substring(idx);
                if (realName.startsWith("block_" + String.format("%3d", SystemConfig.NODE_CURRENT_INDEX).replace(' ', '0')))
                    return true;
                return false;
            }
        });

        System.out.println("Total Files For This Node :" + files.length);


        //start the consumer threads
        IndexBuilderRunnable[] consumers = new IndexBuilderRunnable[WRITE_THREAD];
        for (int i = 0; i < WRITE_THREAD; i++) {
            IndexBuilderRunnable builder = new IndexBuilderRunnable(new BlockingDocumentStream());
            consumers[i] = builder;
            threadSet.add(builder); //threadList.add(builder);
            builder.setListener(this);
            Thread thread = new Thread(builder);
            thread.start();

        }

        //start the producer threads
        List<ArrayList<Path>> paths = new ArrayList<ArrayList<Path>>(FETCH_THREAD);
        for (int i = 0; i < FETCH_THREAD; i++) {
            paths.add(new ArrayList<Path>());
        }

        for (int j = 0; j < files.length; j++) {
            Path p = files[j].getPath();
            System.out.println(p);
            int idx = j % FETCH_THREAD;
            ArrayList<Path> ls = paths.get(idx);
            if (ls == null) {
                ls = new ArrayList<Path>();
            }

            if (!alreadyReadFiles.contains(p.toUri().toString())) {
                ls.add(p);
            }else {
                System.out.println(p.toUri().toString()+" Has Already Stored, Skipped");
            }
        }
        for (ArrayList<Path> path : paths) {
            SequenceFileFetcher fT = new SequenceFileFetcher(consumers, fs, path, conf);
            producerThreadSet.add(fT);
            fT.setListener(this);
            Thread thread = new Thread(fT);
            thread.start();
        }

        System.out.println("waiting for Producers finishing..." + producerThreadSet.size());

        //wait producers
        while (!producerThreadSet.isEmpty()) {
            Thread.sleep(1000);
        }

        System.out.println("Producer finished " + producerThreadSet.size());

        // send end to consumers
        Document doc = new Document();
        doc.setOrgUrlHash(-1);
        for (IndexBuilderRunnable builder : consumers) {
            builder.getDocQueue().getQueue().put(doc);//send end
        }

        //wait consumers
        while (!threadSet.isEmpty()) {
            Thread.sleep(1000);
        }

        writerToAlreadyReadFiles.close();
        long endTime = System.currentTimeMillis();
        System.out.println("Total Dump Time: "+ (endTime-startTime)+"ms");


    }


    @Override
    public synchronized void buildComplete(IndexBuilderRunnable thread) {
        if (threadSet.contains(thread))

            threadSet.remove(thread);
    }

    @Override
    public synchronized void fileReadComplete(SequenceFileFetcher fileFetcher) {
        if (producerThreadSet.contains(fileFetcher)) {
            producerThreadSet.remove(fileFetcher);
        }
        try {
            for (Path path : fileFetcher.getFilesToRead()) {
                if(!alreadyReadFiles.contains(path.toString()))
                    writerToAlreadyReadFiles.write(path.toUri().toString()+"\n");
            }
            writerToAlreadyReadFiles.flush();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
}
