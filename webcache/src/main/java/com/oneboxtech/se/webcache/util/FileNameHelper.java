package com.oneboxtech.se.webcache.util;

import com.oneboxtech.se.webcache.service.SystemConfig;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-7-29
 * Time: 下午4:45
 * To change this template use File | Settings | File Templates.
 */
public class FileNameHelper {

//    static final char[] ALPHABET = {'0','1','2','3','4','5','6','7','8','9'};
    static final char[] ALPHABET = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
//    static final char[] ALPHABET = {'0','1','2','3','4','5','6','7','8','9',
//            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
//            'Q','R','S','T','U','V','W','X','Y','Z'};



    public static String getNameSegment(int hashSegment){
        int t = hashSegment;
        StringBuilder sb = new StringBuilder();
        do{
            sb.append(ALPHABET[t%ALPHABET.length]);
            t= t/ALPHABET.length;
        }while ( t != 0);
        return sb.reverse().toString();
    }

    /**
     *
     * @param urlHash urlHash是url的原始hash值
     * @param appendix 扩展名
     * @return
     */
    public static String getFileFullName(long urlHash, String appendix){
        urlHash &= 0X7FFFFFFFFFFFFFFFL;
//        urlHash /= SystemConfig.NODE_COUNT;

        int dirIdx = (int)(urlHash%SystemConfig.FILE_STORE_DIRS.length);

        urlHash/= SystemConfig.FILE_STORE_DIRS.length;//

        StringBuilder sb = new StringBuilder(SystemConfig.FILE_STORE_DIRS[dirIdx]);

        for (int i : SystemConfig.FILENAME_BASES) {
            int t = (int) (urlHash % i);
            urlHash /= i;
            sb.append("/").append(getNameSegment(t));
        }

        if(appendix != null)
            sb.append(".").append(appendix);
        return sb.toString();
    }
/**
     *
     * @param urlHash urlHash是url的原始hash值
     * @return
     */
    public static String getFileNameWithoutAppendix(long urlHash){
        urlHash &= 0X7FFFFFFFFFFFFFFFL;
//        urlHash /= SystemConfig.NODE_COUNT;

        int dirIdx = (int)(urlHash%SystemConfig.FILE_STORE_DIRS.length);

        urlHash/= SystemConfig.FILE_STORE_DIRS.length;//

        StringBuilder sb = new StringBuilder(SystemConfig.FILE_STORE_DIRS[dirIdx]);

        for (int i : SystemConfig.FILENAME_BASES) {
            int t = (int) (urlHash % i);
            urlHash /= i;
            sb.append("/").append(getNameSegment(t));
        }

        return sb.toString();
    }

//    /**
//     * 检查是否属于该节点
//     * @param orgHash
//     * @return
//     */
//    public static boolean belongsToThisNode(long orgHash){
//        orgHash &= 0X7FFFFFFFFFFFFFFFL;
//        return orgHash%SystemConfig.NODE_COUNT == SystemConfig.NODE_CURRENT_INDEX;
//    }

//    /**
//     * 检测是否属于当前端口的QueryServer进程
//     * @param orgHash
//     * @return
//     */
//    public static boolean belongsToThisPort(long orgHash){
//        orgHash &= 0X7FFFFFFFFFFFFFFFL;
//        if(belongsToThisNode(orgHash)){
//            orgHash/= SystemConfig.NODE_COUNT;
//            return orgHash % SystemConfig.NODE_QUERY_PORTS.length == SystemConfig.NODE_CURRENT_PORT_INDEX;
//        }
//        return false;
//    }


    public static long getDocHashRemain(long orgHash){
        long t = orgHash/SystemConfig.FILE_STORE_DIRS.length;
        for (int i : SystemConfig.FILENAME_BASES) {
            t /= i;
        }
        return t;
    }

}
