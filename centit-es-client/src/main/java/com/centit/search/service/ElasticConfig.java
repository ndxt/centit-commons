package com.centit.search.service;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.security.SecurityOptUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * Created by codefan on 17-6-22.
 */
@Data
public class ElasticConfig {
    private String serverHostIp ;
    private String serverHostPort;
    private String clusterName ;
    private String username ;
    private String password ;
    private float minScore;

    @Setter(AccessLevel.NONE)
    private String[] serverUrls;

    public ElasticConfig(){

    }

    public void setUsername(String username) {
        this.username = SecurityOptUtils.decodeSecurityString(username);;
    }

    public void setPassword(String password) {
        this.password = SecurityOptUtils.decodeSecurityString(password);
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other))
            return true;
        if ((other == null))
            return false;
        if (!(other instanceof ElasticConfig castOther))
            return false;
        return this.getServerHostIp().equals(castOther.getServerHostIp())
            && this.getClusterName().equals(castOther.getClusterName())
            && this.getServerHostPort().equals(castOther.getServerHostPort());
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

    public String[] getServerUrls() {
        if(serverUrls == null || serverUrls.length == 0){
            String[] hosts = serverHostIp.split(",");
            String[] ports = serverHostPort.split(",");
            if(hosts.length > 0){
                serverUrls = new String[hosts.length];
                for(int i = 0; i < hosts.length; i++){
                    int port = 9200;
                    if(ports.length > i){
                        port = NumberBaseOpt.castObjectToInteger(ports[i], port);
                    }
                    serverUrls[i] = "http://" + hosts[i] + ":" + port;
                }
            }
        }
        return serverUrls;
    }
}
