package com.oneboxtech.se.webcache.index;

import com.oneboxtech.se.webcache.exception.BuildIndexException;
import com.oneboxtech.se.webcache.model.*;
import com.oneboxtech.se.webcache.service.SystemConfig;
import com.oneboxtech.se.webcache.util.CommonUtil;
import com.oneboxtech.se.webcache.util.FileNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 8:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexBuilder  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private class FileTuple {
        DocumentFile storeFile;
        IndexFileUnordered indexFileUnordered;

        private FileTuple(DocumentFile storeFile, IndexFileUnordered indexFileUnordered) {
            this.storeFile = storeFile;
            this.indexFileUnordered = indexFileUnordered;
        }
    }

    private static Hashtable<String, FileTuple> step1Files = new Hashtable<String,FileTuple>();

    private static long DOC_NUM = 0L;

    private List<File> uidxFiles;

    private boolean deleteUidxFiles = false;

    private int docCount = 0;
    private int fileCount = 0;

    //初始化


    public IndexBuilder() {
    }

    public void build(IDocumentStream docInput) throws BuildIndexException {
        logger.info("starti building index... generate .store and .uidx files");
        step1(docInput);
    }


//  Please Use IndexConverter To Merge
//    public void merge() throws BuildIndexException {
//        logger.info("step1 finished. starting step2...");
//        for (String dir : SystemConfig.FILE_STORE_DIRS) {
//            logger.info("building index of dir: {}",dir);
//            step2(dir);
//        }
//        logger.info("step2 finished. building finished...");
//
//    }


    /**
     * 第零步，扫描
     */
    private void step0(){

    }

    /**
     * 第一步
     * 将文档流中的文件写入文件系统
     * 创建documentFile和indexFileUnordered
     *
     * @param docInput
     */
    private void step1(IDocumentStream docInput) throws BuildIndexException {
        try {
            Document doc = null;
            while ((doc = docInput.read()) != null) {
                if(doc.getOrgUrlHash() == -1){//-1标示结束
                    logger.info("Received An ending doc, stop read docs");
                    break;
                }

                docCount++;
                DOC_NUM++;
                //System.out.println(doc.getOrgUrlHash());
                //logger.info("Received one doc with hash:{}", String.format("0X%X",doc.getOrgUrlHash()));

                FileTuple ft = step1Files.get(FileNameHelper.getFileFullName (doc.getOrgUrlHash(),"key"));
                if (ft == null) { //如果该文件不存在，创建之
                    fileCount++;
                    File f = new File(FileNameHelper.getFileFullName(doc.getOrgUrlHash(), DocumentFile.APPENDIX));
                    File f2 = new File(FileNameHelper.getFileFullName(doc.getOrgUrlHash(), IndexFileUnordered.APPENDIX));
                    logger.info("creating file" + f.getAbsolutePath());
                    DocumentFile df = new DocumentFile(f);
                    df.openWrite();
                    IndexFileUnordered uidf = new IndexFileUnordered(f2);
                    uidf.openWrite();
                    ft = new FileTuple(df, uidf);
                    step1Files.put(FileNameHelper.getFileFullName(doc.getOrgUrlHash(), "key"), ft);
                }
                int offset = ft.storeFile.getOffset();
                //write doc to document file
//                if(SystemConfig.STORE_GZIP){
//                    int m = doc.getMeta();
//                    m |= Document.META_COMPRESS_GZIP;
//                    doc.setMeta(m);
//                }
                ft.storeFile.writeOne(doc);
                int length = doc.getData().length;
                //写未排序的索引文件
                //ft.indexFileUnordered.writeOne(FileNameHelper.getDocHashRemain(doc.getOrgUrlHash()), offset);
                ft.indexFileUnordered.writeOne((doc.getOrgUrlHash()), offset);//write orgHash

            }
            //closeWrite files
            for (FileTuple tuple : step1Files.values()) {
                if(tuple != null){
                    tuple.storeFile.closeWrite();
                    tuple.indexFileUnordered.closeWrite();
                }
            }
//            step1Files = null;
        } catch (IOException e) {
            e.printStackTrace();
            throw new BuildIndexException(e.getMessage());
        } catch (InterruptedException ee){
            ee.printStackTrace();
            throw new BuildIndexException("interrupted..."+ ee.getMessage());
        }
    }


}
