/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.communications.core.views;

import java.awt.Image;
import org.inventory.communications.util.Utils;


/**
 * This class represents the elements inside a view as recorded in the database
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalObjectView extends LocalObjectViewLight {
    
    /**
     * View structure
     */
    protected byte[] structure;
    /**
     * View background
     */
    protected Image background;
    /**
     * View current zoom
     */
    protected int zoom;
    /**
     * View current center position
     */
    protected double[] center;
    /**
     * Are all the elements is this view exist?
     */
    protected boolean dirty = false;

    public LocalObjectView(long id, String className, String name, String description, byte[] viewStructure, byte[] background) {
        super (id, name, description, className);
        this.background = Utils.getImageFromByteArray(background);
        this.structure = viewStructure;
    }
    
    public byte[] getStructure(){
        return structure;
    }

    public void setStructure(byte[] structure){
        this.structure = structure;
    }
    
    public Image getBackground(){
        return background;
    }

    public void setBackground (Image background){
        this.background = background;
    }

    public double[] getCenter() {
        return center;
    }

    public void setCenter(double[] center) {
        this.center = center;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean isDirty) {
        this.dirty = isDirty;
    }
}
