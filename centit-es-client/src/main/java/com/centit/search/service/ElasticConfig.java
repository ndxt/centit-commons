package com.centit.search.service;

import com.centit.support.security.SecurityOptUtils;
import lombok.Data;

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
    private String usingSSL ;
    private float minScore;

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

}
