package com.centit.search.service;

import com.centit.support.algorithm.NumberBaseOpt;
import org.apache.http.HttpHost;

/**
 * Created by codefan on 17-6-22.
 */
public class ESServerConfig {

    private String osId ;
    private String serverHostIp ;
    private String serverHostPort;
    private String clusterName ;
    private String username ;
    private String password ;
    //#elasticsearch.filter.minScore
    private float minScore;
    private HttpHost[] httpHosts;

    public ESServerConfig(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOsId() {
        return osId;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    public String getServerHostIp() {
        return serverHostIp;
    }

    public void setServerHostIp(String serverHostIp) {
        this.serverHostIp = serverHostIp;
    }

    public String getServerHostPort() {
        return serverHostPort;
    }

    public void setServerHostPort(String serverHostPort) {
        this.serverHostPort = serverHostPort;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /*public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }*/

    public float getMinScore() {
        return minScore;
    }

    public void setMinScore(float minScore) {
        this.minScore = minScore;
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other))
            return true;
        if ((other == null))
            return false;
        if (!(other instanceof ESServerConfig))
            return false;
        ESServerConfig castOther = (ESServerConfig) other;

        return ((this.getServerHostIp() == castOther.getServerHostIp()) || (this
            .getServerHostIp() != null
            && castOther.getServerHostIp() != null && this.getServerHostIp()
            .equals(castOther.getServerHostIp())))
            && ((this.getClusterName() == castOther.getClusterName()) || (this
            .getClusterName() != null
            && castOther.getClusterName() != null && this
            .getClusterName().equals(castOther.getClusterName())))
            && this.getServerHostPort() == castOther.getServerHostPort() ;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result
            + (this.getServerHostIp() == null ? 0 : this.getServerHostIp().hashCode());
        result = 37 * result
            +  Integer.valueOf(this.getServerHostPort()).hashCode();
        result = 37 * result
            + (this.getClusterName() == null ? 0 : this.getClusterName().hashCode());
        return result;
    }

    public HttpHost[] getHttpHosts() {
        if(httpHosts==null || httpHosts.length==0){
            String [] hosts = serverHostIp.split(",");
            String [] ports = serverHostPort.split(",");
            if(hosts.length>0){
                httpHosts = new HttpHost[hosts.length];
                for(int i=0; i<hosts.length; i++){
                    int port = 9300;
                    if(ports.length>i){
                        port = NumberBaseOpt.castObjectToInteger(ports[i],port);
                    }
                    httpHosts[i] = new HttpHost(hosts[i], port);
                }
            }
        }
        return httpHosts;
    }
}
