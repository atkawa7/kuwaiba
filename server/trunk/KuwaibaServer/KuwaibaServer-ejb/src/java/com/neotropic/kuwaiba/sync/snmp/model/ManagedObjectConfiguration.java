/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neotropic.kuwaiba.sync.snmp.model;

/**
 * This class contains all the necessary information to access the information of a managed object (in SNMP, that would be the IP, port and read community)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ManagedObjectConfiguration {
    private String ip;
    private int port;
    private String community;

    public ManagedObjectConfiguration(String ip, int port, String community) {
        this.ip = ip;
        this.port = port;
        this.community = community;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }
}
