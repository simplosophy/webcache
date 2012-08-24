package com.oneboxtech.se.webcache.service;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.oneboxtech.se.webcache.index.IndexReader;
import com.oneboxtech.se.webcache.model.Document;
import com.oneboxtech.se.webcache.model.proto.WebCacheDocProtos;
import com.oneboxtech.se.webcache.util.CommonUtil;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.awt.CharsetString;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-4
 * Time: 上午11:17
 * To change this template use File | Settings | File Templates.
 */
public class HttpQueryServerHandler extends SimpleChannelUpstreamHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private HttpQueryServer server;

    public HttpQueryServerHandler(HttpQueryServer server) {
        this.server = server;
    }




    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (server.monitoringData != null)
            server.monitoringData.increaseServerError();
        logger.error(e.getCause().getMessage());
        e.getCause().printStackTrace();

        e.getChannel().close();
    }

    private GsonBuilder gsonBuilder = new GsonBuilder();

    private JsonObject protoDoc2Json(WebCacheDocProtos.WebCacheDoc doc) {
        JsonObject rtn = null;
        if (doc != null) {
            rtn = new JsonObject();
            rtn.addProperty("errno", 0);
            //rtn.put("version", doc.getVersion());
            long t = doc.getCrawlTime();
            if (t > System.currentTimeMillis() * 1000) {//make sure timestamp is smaller than current
                t = System.currentTimeMillis() * 1000;
            }else if(t < 1331309959000000L){//make sure later than 20120301
                t = 1331309959000000L;
            }
            rtn.addProperty("timestamp", t);
            if (doc.getTitle() != null) {
                try {
                    rtn.addProperty("title", new String(doc.getTitle().toByteArray(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (doc.hasUrl()) {
                rtn.addProperty("url", doc.getUrl());
            }
            if (doc.hasGzipCompressedUtf8Page()) {
                try {
                    String page = new String(Document.gzipUnCompress(doc.getGzipCompressedUtf8Page().toByteArray()), "utf-8");
                    rtn.addProperty("html", page);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (doc.hasUtf8Page()) {
                String page = null;
                try {
                    page = new String((doc.getGzipCompressedUtf8Page().toByteArray()), "utf-8");
                    rtn.addProperty("html", page);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }
        return rtn;
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws java.lang.Exception {
        Object resp = null;
        logger.debug("Received A Request");
        try {
            if (server.monitoringData != null)
                server.monitoringData.increaseAcceptedRequests();
            if (e.getMessage() instanceof HttpRequest) {
                if (server.monitoringData != null)
                    server.monitoringData.increaseHandledRequests();
                HttpRequest req = (HttpRequest) e.getMessage();
                HttpResponse res = null;
                URI uri = new URI(req.getUri());

//                logger.info(server + " Has Received Query:" + uri.getPath());
                String qq = uri.getPath().substring(1);//ignore first '/'
                int slash = qq.indexOf('/');
                if (slash > 0) {
                    qq = qq.substring(0, slash);
                }
                Long q = CommonUtil.tryParseLong(qq);
                if (q == null) {
                    logger.warn(server + " Has Recerved An Wrong Query: " + qq + " it may be overflowed signed long, Plz /Node_Count first");
                    res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                    res.setContent(ChannelBuffers.copiedBuffer("{\"msg\":\"400 Wrong Query\"}", Charset.forName("utf-8")));
                } else {

                    Map<String, String> paras = CommonUtil.parsePara(uri.getQuery());
                    if (paras.containsKey("op")) {//如果是对黑名单进行操作
                        String op = paras.get("op").toLowerCase();
                        String respMsg = "{\"errno\":0}";
                        if (op.equals("blacklist")) {
                            if (server.blackListDocSet != null) {
                                server.blackListDocSet.add(q);
                            }
                        } else if (op.equals("whitelist")) {
                            if (server.blackListDocSet != null) {
                                server.blackListDocSet.remove(q);
                            }
                        }else if(op.equals("dump")){
                            if(server.blackListDocSet!= null){
                                server.blackListDocSet.dump();
                            }
                        }else if(op.equals("clear")){
                            if(server.blackListDocSet!= null){
                                server.blackListDocSet.clear();
                            }
                        }
                        res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                        res.setContent(ChannelBuffers.copiedBuffer( respMsg,Charset.forName("utf-8")));
                    } else {//如果不是对黑名单操作

                        if (!server.blackListDocSet.exist(q)) {
                            int idx = (int) (q % server.readers.length);
                            IndexReader reader = server.readers[idx];
                            if (reader != null) {
                                Document doc = reader.query(q);
                                if (doc != null) {
                                    logger.debug("A Valid Request: " + q);
                                    res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                                    if (server.monitoringData != null)
                                        server.monitoringData.increaseFoundCount();
                                    try {
                                        if (doc.getMeta() == Document.META_GZIPED) {
                                            res.setContent(ChannelBuffers.copiedBuffer(Document.gzipUnCompress(doc.getData())));//uncompress
                                        } else if (doc.getMeta() == Document.META_PROTO_BUF) {//use proto_buf to parse data
                                            WebCacheDocProtos.WebCacheDoc protoDoc = WebCacheDocProtos.WebCacheDoc.parseFrom(doc.getData());
                                            res.setHeader("Content-Type", "application/json;charset=UTF-8"); //return JSON
                                            JsonObject content = protoDoc2Json(protoDoc);
                                            String c = "";
                                            if (content != null) {
                                                Gson gson = gsonBuilder.create();
                                                //c = content.toJSONString();
                                                c = gson.toJson(content);
                                            }
                                            res.setContent(ChannelBuffers.copiedBuffer(c, Charset.forName("utf-8")));
                                        } else {
                                            throw new Exception("Unsupported Document Meta Version");
                                        }
                                    } catch (Exception ee) {
                                        ee.printStackTrace();
                                        if (server.monitoringData != null)
                                            server.monitoringData.increaseServerError();
                                        res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                                        res.setContent(ChannelBuffers.copiedBuffer("{\"msg\":\"500 Internal Server Error, Maybe it's not a valid gzip doc\"}", Charset.forName("utf-8")));
                                        if (ee instanceof ZipException)
                                            logger.error("zip Exception for doc: " + doc.getOrgUrlHash() + " : " + ee.getMessage());
                                        if (ee instanceof InvalidProtocolBufferException)
                                            logger.error("InvalidProtocolBufferException for doc: " + doc.getOrgUrlHash() + " : " + ee.getMessage());

                                    }
                                } else {
                                    if (server.monitoringData != null)
                                        server.monitoringData.increaseNotFoundCount();
                                    logger.info("Document Does Not Exist: " + q);
                                    res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                                    res.setContent(ChannelBuffers.copiedBuffer("{\"msg\":\"404 Document Does Not Exist\"}", Charset.forName("utf-8")));
                                }

                            } else {
                                if (server.monitoringData != null)
                                    server.monitoringData.increaseServerError();
                                logger.warn("Server's IndexReader Is Null ");
                                res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                                res.setContent(ChannelBuffers.copiedBuffer("{\"msg:\":\"500 Server IndexReader Is Null \"}", Charset.forName("utf-8")));

                            }
                        }else {//blacklisted doc
                            if (server.monitoringData != null)
                                        server.monitoringData.increaseFoundCount();
                            logger.info("Document Blacklisted: " + q);
                            res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                            res.setContent(ChannelBuffers.copiedBuffer("{\"msg\":\"404 Document blacklisted\"}", Charset.forName("utf-8")));
                        }


                    }
                }
                resp = res;

            } else {
                logger.warn(server + " Has Received A NON-HTTP Request, Closing Channel");
                resp = "INVALID HTTP REQUEST";
            }
        } catch (Exception ee) {
            ee.printStackTrace();
            //resp = ee.getMessage();
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.setContent(ChannelBuffers.copiedBuffer(ee.getMessage().getBytes()));
            resp = response;
        }

        e.getChannel().write(resp).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                channelFuture.getChannel().close();
            }
        });

    }

}
