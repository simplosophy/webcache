package com.oneboxtech.se.webcache.index;

import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.model.DocumentFile;
import com.oneboxtech.se.webcache.model.InMemIndex;
import com.oneboxtech.se.webcache.model.IndexFile;
import com.oneboxtech.se.webcache.util.CommonUtil;
import com.oneboxtech.se.webcache.util.FileNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 31/7/12
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexReader implements Runnable{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int INIT_MAP_SIZE = 1024;

    private String baseDir;

    private File dir;

    private boolean preOpenStoreFiles = false;

    private HashMap<String,InMemIndex> indexHashMap = new HashMap<String, InMemIndex>(INIT_MAP_SIZE);

    private HashMap<String, DocumentFile> storeMap = new HashMap<String, DocumentFile>(INIT_MAP_SIZE);

    private boolean opened = false;

    private IIndexReaderOpenedListener listener = null;

    public void setListener(IIndexReaderOpenedListener listener){
        this.listener = listener;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public String getBaseDir(){
        return this.baseDir;
    }

    /**
     *
     * @param baseDir 该read的根目录
     */
    public IndexReader(String baseDir) {
        this.baseDir = baseDir;
    }

    public void open() throws IOException {
        logger.info("starting loading index in {}",baseDir);
        dir = new File(baseDir);
        List<File> idxFiles = CommonUtil.listRecursive(dir, IndexFile.APPENDIX);
        logger.info("total .idx count: {}", idxFiles.size());
        for (File idxFile : idxFiles) {
            IndexFile idx = new IndexFile(idxFile);
            idx.openRead();
            InMemIndex im = idx.toMemIndex();
            //String relP = idxFile.getAbsolutePath().substring(SystemConfig.STORE_DIR.length());
            indexHashMap.put(idxFile.getAbsolutePath(),im);//完整路径作为Key
            logger.info("loading index {}", idxFile.getAbsolutePath());
            idx.closeRead();
        }
        logger.info("all .idx are loaded...");
        logger.info("starting locating .store, preOpen? {}", preOpenStoreFiles);
        List<File> storeFiles = CommonUtil.listRecursive(dir, DocumentFile.APPENDIX);
        for (File file : storeFiles) {
            DocumentFile d = new DocumentFile(file);
            storeMap.put(file.getAbsolutePath(), d);
            if(preOpenStoreFiles){
                d.openRead();
            }
        }
        logger.info("all .store are located...");
        this.opened = true;
    }

    public void close() throws IOException {
        for (DocumentFile documentFile : storeMap.values()) {
            documentFile.closeRead();
        }

    }

    public boolean exists(long urlHash){
        if(!opened){
            return false;
        }
        urlHash &= 0X7FFFFFFFFFFFFFFFL;//符号位应为0, java不支持unsigned
        logger.info("querying exists:\t{}",String.format("0X%x",urlHash));
        String idxKeyWithoutAppendix = FileNameHelper.getFileNameWithoutAppendix(urlHash) + ".";
        InMemIndex im = indexHashMap.get(idxKeyWithoutAppendix+IndexFile.APPENDIX);
        if(im == null){
            logger.info("index file for key: {} does not exist", String.format("0X%x",urlHash));
            return false;
        }
        //long remainHash = FileNameHelper.getDocHashRemain(urlHash);
        long remainHash = (urlHash);
        long[] ha = im.getHashArray();
        int idx = Arrays.binarySearch(ha, remainHash);
        if(idx <0){
            logger.info("index item for key: {} does not exist", String.format("0X%x",urlHash));
            return false;
        }

        return true;
    }

    public Document query(long urlHash){
        if(!opened){
            return null;
        }
        urlHash &= 0X7FFFFFFFFFFFFFFFL;//符号位应为0, java不支持unsigned
        logger.debug("querying:\t{}",String.format("0X%x",urlHash));
        String idxKeyWithoutAppendix = FileNameHelper.getFileNameWithoutAppendix(urlHash) + ".";
        InMemIndex im = indexHashMap.get(idxKeyWithoutAppendix+IndexFile.APPENDIX);
        if(im == null){
            logger.info("index file for key: {} does not exist", String.format("0X%x",urlHash));
            return null;
        }
//        long remainHash = FileNameHelper.getDocHashRemain(urlHash);
        long remainHash = (urlHash);  //use org_hash
        long[] ha = im.getHashArray();
        int idx = Arrays.binarySearch(ha, remainHash);
        if(idx <0){
            logger.info("index item for key: {} does not exist", String.format("0X%x",urlHash));
            return null;
        }
        int offset = im.getOffsetArray()[idx];
        String storeFileName = idxKeyWithoutAppendix+DocumentFile.APPENDIX;
        DocumentFile df = storeMap.get(storeFileName);
        if(df == null){
            logger.info("store file for key: {} does not exist", String.format("0X%x",urlHash));
            return null;
        }
        if(!preOpenStoreFiles){
            try {
                df.openRead();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("open file: {} failed", df.getFile().getAbsolutePath());
                return null;
            }
        }
        Document rtn  =  null;
        try {
            rtn = df.readOne(offset);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            logger.error("read from store file: {} failed", df.getFile().getAbsolutePath());
            return null;
        }
        return rtn;
    }

    public boolean isPreOpenStoreFiles() {
        return preOpenStoreFiles;
    }

    public void setPreOpenStoreFiles(boolean preOpenStoreFiles) {
        this.preOpenStoreFiles = preOpenStoreFiles;
    }

    @Override
    public void run() {
        try {
            this.open();
            if(this.listener != null){
                listener.indexOpenFinished(this);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            logger.error("Opening IndexReader of: {} failed.",this.baseDir);
        }
    }
}
