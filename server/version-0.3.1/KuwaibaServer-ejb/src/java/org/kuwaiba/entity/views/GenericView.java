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
package org.kuwaiba.entity.views;

import org.kuwaiba.ws.toserialize.ViewInfo;
import org.kuwaiba.entity.core.ApplicationObject;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import org.kuwaiba.util.HierarchyUtils;

/**
 * Represents a generic object view
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericView extends ApplicationObject{
    /**
     * An XML document containing the view
     */
    @Lob @Basic(fetch=FetchType.LAZY)
    protected byte[] viewStructure;
    /**
     * The view's background
     */
    @Basic(fetch=FetchType.LAZY)
    @Lob 
    protected byte[] background;
    /**
     * Marks the current view as outdated
     */
    protected Boolean outdated;
    /**
     * A short note on the view. This could evolve into sticky notes
     */
    protected String description;

    public GenericView() {
    }

    public GenericView(ViewInfo serializedView) throws UnsupportedOperationException{
        try{
            Class viewClass = Class.forName(serializedView.getViewClass());
            if(HierarchyUtils.isSubclass(viewClass, GenericView.class)){
                this.viewStructure = serializedView.getStructure();
                this.background = serializedView.getBackground();
                this.description = serializedView.getDescription();
            }else throw new UnsupportedOperationException(serializedView.getViewClass());

        }catch(ClassNotFoundException cnfe){
            throw new UnsupportedOperationException(serializedView.getViewClass());
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(Boolean isOutdated) {
        this.outdated = isOutdated;
    }

    public byte[] getViewStructure() {
        return this.viewStructure;
    }

    public void setViewStructure(byte[] structure) {
        this.viewStructure = structure;
    }

    public byte[] getBackground() {
        return background;
    }

    public void setBackground(byte[] background) {
        this.background = background;
    }

}
