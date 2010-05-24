/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.equipment.applicationlayer;

import entity.core.ConfigurationItem;
import entity.multiple.systems.OperatingSystem;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 *
 * @author dib
 */
@Entity
public abstract class GenericApplicationElement extends ConfigurationItem implements Serializable {
    protected String service;
    
    @OneToOne
    protected OperatingSystem operatingSystem;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

}
