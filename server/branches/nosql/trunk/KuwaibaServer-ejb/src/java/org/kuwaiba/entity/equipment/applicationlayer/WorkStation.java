/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kuwaiba.entity.equipment.applicationlayer;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author dib
 */
@Entity
public class WorkStation extends GenericApplicationElement implements Serializable {

    protected String ipAddress;

    public WorkStation() {
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
