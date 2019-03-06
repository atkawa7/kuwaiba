/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.vaadin.lienzo.client.core.shape;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A widget that represents a connection between two nodes
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class SrvEdgeWidget implements Serializable {
    private String id;
    private SrvNodeWidget source;
    private SrvNodeWidget target;
    private String caption;
    private String color = "BLACK";
    private List<Point> controlPoints;
    private boolean editable = true;

    public SrvEdgeWidget() { }
        
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public SrvNodeWidget getSource() {
        return source;
    }
    
    public void setSource(SrvNodeWidget source) {
        this.source = source;
    }
    
    public SrvNodeWidget getTarget() {
        return target;
    }
    
    public void setTarget(SrvNodeWidget target) {
        this.target = target;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public List<Point> getControlPoints() {
        return controlPoints;
    }
    
    public void setControlPoints(List<Point> controlPoints) {
        this.controlPoints = controlPoints;        
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
        
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SrvEdgeWidget other = (SrvEdgeWidget) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
}
