package com.oneboxtech.se.webcache.index;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-4
 * Time: 上午10:01
 * To change this template use File | Settings | File Templates.
 */

import com.oneboxtech.se.webcache.model.IndexFile;
import com.oneboxtech.se.webcache.model.IndexFileItem;
import com.oneboxtech.se.webcache.model.IndexFileUnordered;
import com.oneboxtech.se.webcache.service.SystemConfig;
import com.oneboxtech.se.webcache.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Convert .uidx files to .idx files
 */
public class IndexConverter implements Runnable{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public  boolean deleteUidx = false;

    private String baseDir;

    public IndexConverter(String baseDir,boolean  deleteUidx) {
        this.baseDir = baseDir;
        this.deleteUidx =deleteUidx;
    }

    public void run(){
        File f = new File(baseDir);
        if(!f.exists()){
            logger.warn( baseDir+ " does not exist. skip.");
            return;
        }
        List<File> uidx ;
        if(f.isFile()){
            if(f.getName().endsWith( "."+IndexFileUnordered.APPENDIX)){
                uidx = new ArrayList<File>();
                uidx.add(f);
            }else {
                logger.warn(baseDir+" is not an .uidx file");
                return;
            }
        }else {
            uidx = CommonUtil.listRecursive(f,IndexFileUnordered.APPENDIX) ;
        }
        for(int i=0; i<uidx.size();i++){
            File ff = uidx.get(i);
            logger.info(String.format("processing [%d/%d] : %s",i+1,uidx.size(),ff.getAbsolutePath()));
            try {
                String fname = ff.getAbsolutePath();
                List<IndexFileItem> items = IndexFileUnordered.readAllFromFile(ff.getAbsolutePath());
                Collections.sort(items);//sort
                int j = fname.lastIndexOf('.');
                File idxFile = new File(fname.substring(0, j) + "." + IndexFile.APPENDIX);
                IndexFile indexFile = new IndexFile(idxFile);
                IndexFile.writeAll(items, indexFile);
                if(deleteUidx){
                    ff.delete();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {
//        if(args.length < 1 ){
//            System.out.println("usage: java com.oneboxtech.se.webcache.index.IndexConverter  [baseDir] [true|false]");
//            return;
//        }
//        boolean  del = false;
//        if(args.length>1){
//            del = Boolean.parseBoolean(args[1].toLowerCase()) ;
//        }

        for (String fileStoreDir : SystemConfig.FILE_STORE_DIRS) {
            IndexConverter converter = new IndexConverter(fileStoreDir,false);
            new Thread(converter).start();
        }


    }

}
