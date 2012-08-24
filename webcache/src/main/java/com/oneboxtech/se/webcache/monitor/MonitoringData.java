package com.oneboxtech.se.webcache.monitor;

/**
 * Created with IntelliJ IDEA.
 * User: shangrenxiang
 * Date: 12-8-8
 * Time: 上午11:01
 * To change this template use File | Settings | File Templates.
 */
public class MonitoringData {
    private String groupName = "webcache";
    private long acceptedRequests=0;
    private long handledRequests=0;
    private long foundCount=0;
    private long notFoundCount=0;
    private long serverError=0;


    public String toString(){
        StringBuilder sb = new StringBuilder() ;
        sb.append(groupName).append("\t").append("accepted_requests").append("\t").append(acceptedRequests).append("\n");
        sb.append(groupName).append("\t").append("handled_requests").append("\t").append(handledRequests).append("\n");
        sb.append(groupName).append("\t").append("found_count").append("\t").append(foundCount).append("\n");
        sb.append(groupName).append("\t").append("not_found_count").append("\t").append(notFoundCount).append("\n");
        sb.append(groupName).append("\t").append("server_error").append("\t").append(serverError).append("\n");
        return sb.toString();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getAcceptedRequests() {
        return acceptedRequests;
    }

    public void increaseAcceptedRequests() {

        this.acceptedRequests ++;
    }

    public long getHandledRequests() {
        return handledRequests;
    }

    public void increaseHandledRequests() {
        this.handledRequests++;
    }

    public long getFoundCount() {
        return foundCount;
    }

    public void increaseFoundCount() {
        this.foundCount++;
    }

    public long getNotFoundCount() {
        return notFoundCount;
    }

    public void increaseNotFoundCount() {
        this.notFoundCount++;
    }

    public long getServerError() {
        return serverError;
    }

    public void increaseServerError() {
        this.serverError++;
    }
}
