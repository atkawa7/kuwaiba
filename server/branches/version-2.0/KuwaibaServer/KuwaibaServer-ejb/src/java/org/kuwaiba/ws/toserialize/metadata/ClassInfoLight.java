/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import java.io.Serializable;
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
public class ClassInfoLight implements Serializable {
    protected long id;
    protected Boolean _abstract;
    protected Boolean viewable;
    protected Boolean custom;
    protected Boolean inDesign;
    protected Boolean listType;
    protected Validator[] validators;
    protected String className;
    protected String displayName;
    protected String parentClassName;
    protected int color;
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
        this._abstract = myClassLight.isAbstract();
        this.displayName = myClassLight.getDisplayName();
        this.validators = validators;
        this.viewable = myClassLight.isViewable();
        this.listType = myClassLight.isListType();
        this.custom = myClassLight.isCustom();
        this.inDesign = myClassLight.isInDesign();
        this.color = myClassLight.getColor();
    }

    public ClassInfoLight (long id, String className, String displayName, 
            Validator[] validators, boolean viewable, boolean _abstract, boolean custom, 
            boolean inDesign, String parentClassName, boolean listType, byte[] smallIcon, int color){
        this.id = id;
        this.className = className;
        this.displayName = displayName;
        this.viewable = viewable;
        this._abstract = _abstract;
        this.custom = custom;
        this.inDesign = inDesign;
        this.parentClassName = parentClassName;
        this.validators = validators;
        this.smallIcon = smallIcon;
        this.listType = listType;
        this.color = color;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    public Validator[] getValidators() {
        return validators;
    }

    public void setValidators(Validator[] validators) {
        this.validators = validators;
    }

    public Boolean isAbstract() {
        return _abstract;
    }

    public void setAbstract(Boolean _abstract) {
        this._abstract = _abstract;
    }

    public Boolean isViewable() {
        return viewable;
    }

    public void setViewable(Boolean viewable) {
        this.viewable = viewable;
    }

    public Boolean isCustom() {
        return custom;
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }

    public Boolean isInDesign() {
        return inDesign;
    }

    public void setInDesign(Boolean inDesign) {
        this.inDesign = inDesign;
    }

    public Boolean isListType() {
        return listType;
    }

    public void setListType(Boolean listType) {
        this.listType = listType;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof ClassInfoLight))
            return false;
        if (((ClassInfoLight)obj).getId() == getId())
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 47 * hash + (this.className != null ? this.className.hashCode() : 0);
        return hash;
    }
}
