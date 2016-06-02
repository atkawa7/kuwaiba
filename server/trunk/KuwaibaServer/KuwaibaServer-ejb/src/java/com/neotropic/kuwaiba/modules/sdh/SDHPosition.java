/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.sdh;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * It's a simple class representing a single position used by a container within a transport link
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SDHPosition implements Serializable {
    /**
     * Id of the connection being used (a TransportLink or a ContainerLink)
     */
    private long connectionId;
    /**
     * Id of the connection being used (a TransportLink or a ContainerLink)
     */
    private String connectionClass;
    /**
     * Actual position (STM timeslot or VC4 timeslot)
     */
    private int position;

    public SDHPosition(String connectionClass, long connectionId, int position) {
        this.connectionId = connectionId;
        this.connectionClass = connectionClass;
        this.position = position;
    }

    public long getLinkId() {
        return connectionId;
    }

    public void setLinkId(long connectionId) {
        this.connectionId = connectionId;
    }

    public String getLinkClass() {
        return connectionClass;
    }

    public void setLinkClass(String linkClass) {
        this.connectionClass = linkClass;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
