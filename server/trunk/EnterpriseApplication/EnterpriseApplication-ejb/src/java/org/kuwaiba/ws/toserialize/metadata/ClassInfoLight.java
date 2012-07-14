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
package org.kuwaiba.ws.toserialize.metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.ws.toserialize.application.Validator;

/**
 * Same as ClassInfo, but lighter, since it's intended to provide the information to
 * render a node in a view (usually a tree) at client side.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassInfoLight {
    protected Long id;
    protected Boolean abstractClass;
    protected Boolean viewable;
    protected Boolean listType;
    protected Validator[] validators;
    protected String className;
    protected String displayName;
    protected String parentClassName;
    /**
     * 16x16 icon
     */
    protected byte[] smallIcon;

    public ClassInfoLight(){}

    public ClassInfoLight(ClassMetadataLight myClassLight, Validator[] validators) {
        this.id = myClassLight.getId();
        this.className = myClassLight.getName();
        this.parentClassName = myClassLight.getParentClassName();
        this.smallIcon = myClassLight.getSmallIcon();
        this.abstractClass = myClassLight.isAbstractClass();
        this.displayName = myClassLight.getDisplayName();
        this.validators = validators;
        this.viewable = myClassLight.isViewable();
        this.listType = myClassLight.isListType();
    }

    public ClassInfoLight(Long id, String name, String displayName,Validator[] validators, boolean isViewable,
            boolean isAbstract, boolean isListType, byte[] smallIcon) {
        this.id = id;
        this.abstractClass = isAbstract;
        this.validators = validators;
        this.viewable = isViewable;
        this.className = name;
        this.displayName = displayName;
        this.smallIcon = smallIcon;
        this.listType = isListType;
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

    public Boolean getAbstractClass() {
        return abstractClass;
    }

    public void setAbstractClass(Boolean abstractClass) {
        this.abstractClass = abstractClass;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }
    
    public Boolean isViewable() {
        return viewable;
    }

    public void setViewable(Boolean viewable) {
        this.viewable = viewable;
    }

    public Validator[] getValidators() {
        return validators;
    }

    public void setValidators(Validator[] validators) {
        this.validators = validators;
    }

    public Boolean getListType() {
        return listType;
    }

    public void setListType(Boolean listType) {
        this.listType = listType;
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
