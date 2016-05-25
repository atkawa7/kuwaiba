/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.sdh;

import java.io.Serializable;
import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
  * Instances of this class define a tributary link
  * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
*/
public class SDHTributaryLinkDefinition implements Serializable {
    /**
     * Link object
     */
    private RemoteBusinessObjectLight link;

    /**
     * The positions used by the container
     */
    private List<SDHPosition> positions;

    public SDHTributaryLinkDefinition(RemoteBusinessObjectLight link, List<SDHPosition> positions) {
        this.link = link;
        this.positions = positions;
    }       

    public RemoteBusinessObjectLight getContainerName() {
        return link;
    }

    public void setContainerName(RemoteBusinessObjectLight link) {
        this.link = link;
    }

    public List<SDHPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<SDHPosition> positions) {
        this.positions = positions;
    }
}
