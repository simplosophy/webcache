package com.oneboxtech.se.webcache.model;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-18
 * Time: 下午3:33
 * To change this template use File | Settings | File Templates.
 */
public class BlackListFile {
    private File file;
    private DataInputStream input;
    private DataOutputStream output;

    public BlackListFile(File file){
        this.file = file;
    }

    public void writeOne(long l) throws IOException {
        output.writeLong(l);
    }

    public long readOne() throws IOException {
        return input.readLong();
    }

    public void openRead() throws IOException {
        if(output != null){
            throw new IOException("Output Stream is not null, Close Write before opening read");
        }
        if(input == null){
            input = new DataInputStream(new FileInputStream(file));
        }
    }

    public void closeRead() throws IOException {
        if(input != null){
            input.close();
            input = null;
        }
    }

    public void openWrite() throws IOException {
        if(input != null){
            throw new IOException("Input Stream is not null, Close Read before opening write");
        }
        if(output == null){
            output = new DataOutputStream(new FileOutputStream(file));
        }
    }

    public void closeWrite() throws IOException {
        if(output != null){
            output.close();
            output=null;
        }
    }

}
