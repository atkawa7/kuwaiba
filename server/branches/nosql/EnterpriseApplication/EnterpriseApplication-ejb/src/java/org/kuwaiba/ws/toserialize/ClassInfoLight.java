/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.kuwaiba.ws.toserialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Same as ClassInfo, but lighter, since it's intended to provide the information to
 * render a node in a view (usually a tree) at client side.
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassInfoLight {
    protected Long id;
    protected Boolean abstractClass;
    protected Boolean physicalNode;
    protected Boolean physicalConnection;
    protected Boolean physicalEndpoint;
    protected Boolean viewable;
    protected String className;
    protected String displayName;
    /**
     * 16x16 icon
     */
    protected byte[] smallIcon;

    public ClassInfoLight(){}

    public ClassInfoLight(Long id, String name, String displayName,boolean isPhysicalNode,
            boolean isPhysicalConnection,boolean isPhysicalEndpoint, boolean isViewable,
            boolean isAbstract, byte[] smallIcon) {
        this.id = id;
        this.abstractClass = isAbstract;
        this.physicalNode = isPhysicalNode;
        this.physicalConnection = isPhysicalConnection;
        this.physicalEndpoint = isPhysicalEndpoint;
        this.viewable = isViewable;
        this.className = name;
        this.displayName = displayName;
        this.smallIcon = smallIcon;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isAbstract() {
        return abstractClass;
    }

    public void setIsAbstract(Boolean isAbstract) {
        this.abstractClass = isAbstract;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(byte[] smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Boolean isPhysicalConnection() {
        return physicalConnection;
    }

    public void setPhysicalConnection(Boolean isPhysicalConnection) {
        this.physicalConnection = isPhysicalConnection;
    }

    public Boolean isPhysicalEndpoint() {
        return physicalEndpoint;
    }

    public void setPhysicalEndpoint(Boolean isPhysicalEndpoint) {
        this.physicalEndpoint = isPhysicalEndpoint;
    }

    public Boolean isPhysicalNode() {
        return physicalNode;
    }

    public void setPhysicalNode(Boolean isPhysicalNode) {
        this.physicalNode = isPhysicalNode;
    }

    public Boolean isViewable() {
        return viewable;
    }

    public void setViewable(Boolean viewable) {
        this.viewable = viewable;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof ClassInfoLight))
            return false;
        if (((ClassInfoLight)obj).getId().longValue() == getId().longValue())
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
