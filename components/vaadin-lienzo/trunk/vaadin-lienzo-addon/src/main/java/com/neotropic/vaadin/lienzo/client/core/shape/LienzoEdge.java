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

/**
 * 
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class LienzoEdge implements Serializable {
    private LienzoNode source;
    private LienzoNode target;
    private String caption;
    private String color;
    
    public LienzoEdge() {
    }
    
    public LienzoNode getSource() {
        return source;
    }
    
    public void setSource(LienzoNode source) {
        this.source = source;
    }
    
    public LienzoNode getTarget() {
        return target;
    }
    
    public void setTarget(LienzoNode target) {
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
}
