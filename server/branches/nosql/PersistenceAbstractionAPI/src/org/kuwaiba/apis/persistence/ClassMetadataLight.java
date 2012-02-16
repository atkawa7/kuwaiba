/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence;

import java.io.Serializable;

/**
 * Contains the basic metadata information about a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassMetadataLight implements Serializable{
    protected Long id;
    protected Boolean abstractClass;
    protected Boolean physicalNode;
    protected Boolean physicalConnection;
    protected Boolean physicalEndpoint;
    protected Boolean viewable;
    protected String className;
    protected String displayName;
    protected byte[] smallIcon;

    public ClassMetadataLight() {
        this.id = new Long (5);
        this.className = "HolaXXXXXXXXXXXXXXXX";
    }



    public Boolean getAbstractClass() {
        return abstractClass;
    }

    public void setAbstractClass(Boolean abstractClass) {
        this.abstractClass = abstractClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getPhysicalConnection() {
        return physicalConnection;
    }

    public void setPhysicalConnection(Boolean physicalConnection) {
        this.physicalConnection = physicalConnection;
    }

    public Boolean getPhysicalEndpoint() {
        return physicalEndpoint;
    }

    public void setPhysicalEndpoint(Boolean physicalEndpoint) {
        this.physicalEndpoint = physicalEndpoint;
    }

    public Boolean getPhysicalNode() {
        return physicalNode;
    }

    public void setPhysicalNode(Boolean physicalNode) {
        this.physicalNode = physicalNode;
    }

    public byte[] getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(byte[] smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Boolean getViewable() {
        return viewable;
    }

    public void setViewable(Boolean viewable) {
        this.viewable = viewable;
    }
}
