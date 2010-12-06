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
package entity.views;

import core.annotations.Hidden;
import core.toserialize.ViewInfo;
import entity.core.RootObject;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import util.HierarchyUtils;

/**
 * Represents a generic object view
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Hidden
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericView extends RootObject{
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
    protected Boolean isOutdated;
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

    public Boolean getIsOutdated() {
        return isOutdated;
    }

    public void setIsOutdated(Boolean isOutdated) {
        this.isOutdated = isOutdated;
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
