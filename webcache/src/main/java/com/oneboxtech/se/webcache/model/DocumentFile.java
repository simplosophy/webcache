package com.oneboxtech.se.webcache.model;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;

/**
 * 保存页面内容的文件
 */
public class DocumentFile {

    public static final String APPENDIX = "store";

    private File file;

    /**
     * 写文件时使用
     */
    private RandomAccessFile output;

    /**
     * 读文件时使用
     */
    private RandomAccessFile input;

    private int offset = 0;

    public DocumentFile(File file) {
        this.file = file;
    }

    public void openRead() throws IOException {
        if(output != null){
            throw new IOException("该文件正被写入");
        }
        if(!file.exists()){
            throw new IOException("文件不存在:"+file.getAbsolutePath());
        }
        if(input == null){
            input = new RandomAccessFile(file,"r" );
            offset = 0;
        }
    }

    public void closeRead() throws IOException {
        if(input != null){
            input.close();
        }
    }

    public void openWrite() throws IOException {
        if(input != null){
            throw new IOException("该文件正被读取");
        }
        if(!file.exists()){
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        if(output == null){
            output = new RandomAccessFile( file,"rw");
            output.seek(output.length()); //到尾巴处，追加内容 todo 恢复现场
            offset = (int) output.length();
        }
    }

    public synchronized void closeWrite() throws IOException {
        if(output != null){
            output.close();
            //output = null;
        }
    }

    public Document readOne(int offset) throws IOException {
        if(input == null){
            throw  new IOException("输入流为空，openRead before read from a stream");
        }
        return Document.readOne(input, offset);
    }

    public void writeOne(Document doc) throws IOException {
        if(output == null){
            throw  new IOException("输出流为空，openWrite before write to a stream");
        }
        doc.writeOne(output);
        offset += doc.getRealLength();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
