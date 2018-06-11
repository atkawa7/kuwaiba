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
import java.util.ArrayList;
import java.util.List;

/**
 * The Class representing a Lienzo Shape
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class ServerElement implements Serializable {
    private long id;
    private ServerElement parent;
    private List<ServerElement> children;
    /**
     * X position.
     */
    private double x;
    /**
     * Y position.
     */
    private double y;
    /**
     * Width
     */
    private double width;
    /**
     * Height
     */
    private double height;
    /**
     * Is the shape resizeable? Default value is false.
     */
    private boolean resizable;
    /**
     * Is the shape draggable? Default value true.
     */
    private boolean draggable;   
    
    public ServerElement() {
        this.resizable = false;
        this.draggable = true;
    }
    
    public ServerElement(long id) {
        this();
        this.id = id;
    }
    
    public ServerElement(long id, ServerElement parent) {
        this(id);
        this.parent = parent;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public ServerElement getParent() {
        return parent;
    }
    
    public void setParent(ServerElement parent) {
        this.parent = parent;
    }
    
    public void addChild(ServerElement child) {
        if (children == null)
            children = new ArrayList();
                
        children.add(child);
    }
    
    public void removeChild(ServerElement child) {
        if (children != null)
            children.remove(child);
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getWidth() {
        return width;        
    }
    
    public void setWidth(double width) {
        this.width = width;        
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public boolean isResizable() {
        return resizable;
    }
    
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }
    
    public boolean isDraggable() {
        return draggable;
    }
    
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        return getId() == ((ServerElement) obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
