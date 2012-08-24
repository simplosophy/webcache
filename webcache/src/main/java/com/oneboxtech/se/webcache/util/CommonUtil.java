/**
 *
 */
package com.oneboxtech.se.webcache.util;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author flying
 */
public class CommonUtil {

    public static Map<String,String> parsePara(String query){
        Map<String, String> map = new HashMap<String, String>();
        if(query == null)
            return map;
        String[] params = query.split("&");
        for (String param : params)
        {
            String[] kvs = param.split("=");
            if(kvs.length>1){
               map.put(kvs[0],kvs[1]);
            }else if(kvs.length==1){
               map.put(kvs[0],"");
            }
        }
        return map;
    }    /**
     * @param s 返回null，如果parse失败
     * @return
     */
    public static Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static int[] tryParseIntArray(String s, String separatorRegex) {
        String[] ss = s.split(separatorRegex);
        int[] rtn = new int[ss.length];
        for (int i = 0; i < rtn.length; i++) {
            rtn[i] = tryParseInt(ss[i]);
        }
        return rtn;
    }


    public static String join(Collection<?> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }


    public static Long tryParseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }

//	public static Map<String, Integer> parseRight(String right){
//		if(right == null) return null;
//		Map<String, Integer> rtn = new HashMap<String, Integer>();
//		try {
//			rtn = objMapper.readValue(right, Map.class);
//		} catch (JsonParseException e) {
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return rtn;
//	}




    /**
     * 提取出inputstream中bytes，主要用于将图片文件转化为字节序列，然后提取出验证码
     *
     * @param is
     * @return
     * @throws java.io.IOException
     */
    public static byte[] inputstream2Bytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int ch;
        try {
            while ((ch = is.read()) != -1) {
                baos.write(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
        byte[] rtn = baos.toByteArray();
        baos.close();
        is.close();
        return rtn;
    }

    public static Timestamp parseTimestamp(String orderDate) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return new Timestamp(dateFormat.parse(orderDate).getTime());
    }


    public static String fileAsString(File f, String encoding){
        if(encoding == null){
            encoding = "utf-8";
        }
        try {
            InputStream ins = new FileInputStream(f);
            BufferedReader r = new BufferedReader(new InputStreamReader(ins, encoding));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = r.readLine()) != null)
                sb.append(line).append("\r\n");
            r.close();
            ins.close();
            String s = sb.toString();
            return s;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

    /**
     * 从map中获取value，若不存在，返回null
     *
     * @param map
     * @param key
     * @return
     */
    public static <K, V> V getValueFromMap(Map<K, V> map, K key) {
        if (!map.containsKey(key))
            return null;
        return map.get(key);
    }


    private static void listFile(File f, List<File> result, String appendix){
        if(f == null) return;
            if(f.isFile())
                if(f.getName().endsWith("." +appendix)){  //don't forget "."
                    result.add(f);
                    return;
                }

            File[] sub = f.listFiles();
            if(sub != null)
            for (File file : sub) {

                listFile(file, result, appendix);
            }


    }

    public static List<File> listRecursive(File base, String appendix){
        List<File> rtn = new ArrayList<File>();
        listFile(base, rtn,appendix);
        return rtn;
    }


}
