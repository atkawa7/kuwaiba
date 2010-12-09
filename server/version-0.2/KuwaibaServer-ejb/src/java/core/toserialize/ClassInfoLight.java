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
    protected String _package;
    protected String displayName;
    protected String description;
    protected byte[] smallIcon;

    public ClassInfoLight(){}

    public ClassInfoLight(ClassMetadata cm) {
        this.id = cm.getId();
        this.isAbstract = cm.getIsAbstract();
        this.isPhysicalNode = cm.getIsPhysicalNode();
        this.isPhysicalConnection = cm.getIsPhysicalConnection();
        this.isPhysicalEndpoint = cm.getIsPhysicalEndpoint();
        this.className = cm.getName();
        this._package = cm.getPackageInfo().getName();
        this.displayName = cm.getDisplayName();
        this.description = cm.getDescription();
        this.smallIcon = cm.getSmallIcon();
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

    public String getPackage() {
        return _package;
    }

    public void setPackage(String packageName) {
        this._package = packageName;
    }

    public Boolean getIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(Boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
