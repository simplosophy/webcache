package com.oneboxtech.se.webcache.model;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 建立索引初期的未经过排序的索引文件
 */
public class IndexFileUnordered {

    public static final String APPENDIX = "uidx";

    private int version;

    private int meta;

    private File file;

    private RandomAccessFile outputStream;

    public IndexFileUnordered(File file) {
        this.file = file;
    }

    public void openWrite() throws IOException {
        if(!file.exists()){
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        outputStream = new RandomAccessFile(file, "rw");
        if(outputStream.length() == 0){
            outputStream.writeInt(version);
            outputStream.writeInt(meta);
        }
        else {  //go to  end of file
            int round = (int) ((outputStream.length()-8)%(8+4));
            outputStream.seek(outputStream.length() - round); //
        }
    }

    public void closeWrite() throws IOException {
        outputStream.close();
    }

    public void writeOne(long presentHash, int offset) throws IOException {
        outputStream.writeLong(presentHash);
        outputStream.writeInt(offset);
        //outputStream.writeInt(length);
    }

    public static List<IndexFileItem> readAllFromFile(String uidxFilePath) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(new File(uidxFilePath)));
        ArrayList<IndexFileItem> rtn = null;
        try{
            int version = in.readInt();
            int meta = in.readInt();
            rtn = new ArrayList<IndexFileItem>();
            while (true){
                IndexFileItem item = new IndexFileItem();
                item.setHash(in.readLong());
                item.setOffset(in.readInt());
                rtn.add(item);
            }
        }catch (EOFException e){

        }
        return rtn;
    }


    public RandomAccessFile getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(RandomAccessFile outputStream) {
        this.outputStream = outputStream;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
