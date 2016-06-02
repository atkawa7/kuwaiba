/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.sdh;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 * Instances of this class define a container
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SDHContainerLinkDefinition implements Serializable {
    /**
     * Container object
     */
    private RemoteBusinessObjectLight container;

    /**
     * Is this container structured?
     */
    private boolean structured;
    /**
     * The positions used by the container
     */
    private List<SDHPosition> positions;

    public SDHContainerLinkDefinition(RemoteBusinessObjectLight container, boolean structured, List<SDHPosition> positions) {
        this.container = container;
        this.structured = structured;
        this.positions = positions;
    }       

    public RemoteBusinessObjectLight getContainerName() {
        return container;
    }

    public void setContainerName(RemoteBusinessObjectLight container) {
        this.container = container;
    }

    public List<SDHPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<SDHPosition> positions) {
        this.positions = positions;
    }

    public RemoteBusinessObjectLight getContainer() {
        return container;
    }

    public boolean isStructured() {
        return structured;
    }        
}
