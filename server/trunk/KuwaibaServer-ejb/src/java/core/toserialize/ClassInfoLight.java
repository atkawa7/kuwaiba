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
package core.toserialize;

import entity.core.metamodel.ClassMetadata;
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
    protected Boolean isAbstract;
    protected Boolean isPhysicalNode;
    protected Boolean isPhysicalConnection;
    protected Boolean isPhysicalEndpoint;
    protected String className;
    protected String displayName;
    protected byte[] smallIcon;

    public ClassInfoLight(){}

    public ClassInfoLight(ClassMetadata cm) {
        this.id = cm.getId();
        this.isAbstract = cm.isAbstract();
        this.isPhysicalNode = cm.isPhysicalNode();
        this.isPhysicalConnection = cm.isPhysicalConnection();
        this.isPhysicalEndpoint = cm.isPhysicalEndpoint();
        this.className = cm.getName();
        this.displayName = cm.getDisplayName();
        this.smallIcon = cm.getSmallIcon();
    }

    public ClassInfoLight(Long id, String name, String displayName,boolean isAbstract,
                        boolean isPhysicalNode, boolean isPhysicalConnection,
                        boolean isPhysicalEndpoint, byte[] smallIcon){
        this.id = id;
        this.isAbstract = isAbstract;
        this.isPhysicalNode = isPhysicalNode;
        this.isPhysicalConnection = isPhysicalConnection;
        this.isPhysicalEndpoint = isPhysicalEndpoint;
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

    public Boolean getIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(Boolean isAbstract) {
        this.isAbstract = isAbstract;
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

    public Boolean getIsPhysicalConnection() {
        return isPhysicalConnection;
    }

    public void setIsPhysicalConnection(Boolean isPhysicalConnection) {
        this.isPhysicalConnection = isPhysicalConnection;
    }

    public Boolean getIsPhysicalEndpoint() {
        return isPhysicalEndpoint;
    }

    public void setIsPhysicalEndpoint(Boolean isPhysicalEndpoint) {
        this.isPhysicalEndpoint = isPhysicalEndpoint;
    }

    public Boolean getIsPhysicalNode() {
        return isPhysicalNode;
    }

    public void setIsPhysicalNode(Boolean isPhysicalNode) {
        this.isPhysicalNode = isPhysicalNode;
    }
}
