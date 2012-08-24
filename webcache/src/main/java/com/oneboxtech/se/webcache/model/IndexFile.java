package com.oneboxtech.se.webcache.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: flying
 * Date: 30/7/12
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */

public class IndexFile {

    public static final String APPENDIX = "idx";

    private int meta;

    private int version;

    private File file;

    private DataInputStream input;

    public void openRead() throws FileNotFoundException {
        if(input== null)
            input = new DataInputStream(new FileInputStream(file));
    }

    public void closeRead() throws IOException {
        if(input != null)
            input.close();
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public IndexFile(File file) {

        this.file = file;
    }

    public static void writeAll(List<IndexFileItem> orderedIndexItems, IndexFile idxF) throws IOException {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(idxF.file));
        out.writeInt(idxF.getVersion());
        out.writeInt(idxF.getMeta());
        for (IndexFileItem item : orderedIndexItems) {
            out.writeLong(item.getHash());
            out.writeInt(item.getOffset());
        }

    }

    /**
     * openRead先
     * @return
     * @throws IOException
     */
    public InMemIndex toMemIndex() throws IOException {
        if(input == null){
            throw new IOException("openRead first");
        }
        InMemIndex rtn = new InMemIndex();

        this.version = input.readInt();
        this.meta = input.readInt();

        ArrayList<Long> hashLs = new ArrayList<Long>();
        ArrayList<Integer> offsetLs = new ArrayList<Integer>();
        //read all
        try{
            while (true){
                long hash = input.readLong();
                int offset = input.readInt();
                hashLs.add(hash);
                offsetLs.add(offset);
            }
        }catch (EOFException e){}

        long[] hashArr = new long[hashLs.size()];
        int[] offsetArr = new int[offsetLs.size()];
        for (int i=0; i<hashArr.length;i++) {
            hashArr[i] = hashLs.get(i);
        }
        for (int i=0 ; i< offsetArr.length;i++){
            offsetArr[i] = offsetLs.get(i);
        }

        hashLs = null;
        offsetLs = null;
        System.gc();

        rtn.setHashArray(hashArr);
        rtn.setOffsetArray(offsetArr);
        rtn.setVersion(version);
        rtn.setMeta(meta);

        return rtn;
    }

}
