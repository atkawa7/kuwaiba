/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.visualization.views;

import com.neotropic.flow.component.mxgraph.MxGraphNode;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;

/**
 * Represents a block in a rack view. Stores the MxgraphNode that encloses one
 * rack unit or more in the case of devices using more than one rack unit
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class RackSegment {
    
    private MxGraphNode segmentNode;
    private int initialPosition;
    private int finalPosition;
    private List<BusinessObject> devices;

    public int getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(int initialPosition) {
        this.initialPosition = initialPosition;
    }

    public int getFinalPosition() {
        return finalPosition;
    }

    public void setFinalPosition(int finalPosition) {
        this.finalPosition = finalPosition;
    }

    public List<BusinessObject> getDevices() {
        return devices;
    }

    public void setDevices(List<BusinessObject> devices) {
        this.devices = devices;
    }

    public MxGraphNode getSegmentNode() {
        return segmentNode;
    }

    public RackSegment() {
    } 
    
    public void setSegmentNode(MxGraphNode segmentNode) {
        this.segmentNode = segmentNode;
    }

    public RackSegment(MxGraphNode segmentNode, int initialPosition, int finalPosition, List<BusinessObject> devices) {
        this.segmentNode = segmentNode;
        this.initialPosition = initialPosition;
        this.finalPosition = finalPosition;
        this.devices = devices;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if((o instanceof MxGraphNode))       
            return segmentNode == null ? false : segmentNode.equals(o);
        if(!(o instanceof RackSegment))
            return false;
        return segmentNode == null ? false : segmentNode.equals(((RackSegment)o).getSegmentNode());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.segmentNode);
        return hash;
    }

    @Override
    public String toString() {
        return initialPosition + "-" + finalPosition + " -> " + devices; 
    }
    
    
    
    

}
