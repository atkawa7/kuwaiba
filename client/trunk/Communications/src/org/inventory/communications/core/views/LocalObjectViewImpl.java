/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.core.services.utils.Utils;
import org.openide.util.lookup.ServiceProvider;


/**
 * This class represents the elements inside a view as recorded in the database
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=LocalObjectView.class)
public class LocalObjectViewImpl  extends LocalObjectViewLightImpl implements LocalObjectView {
    /**
     * XML view structure
     */
    private byte[] viewStructure;
    /**
     * The view background
     */
    private Image background;
    private int zoom;
    private double[] center;
    
    public LocalObjectViewImpl() {
    }

    public LocalObjectViewImpl(long id, String name, String description, int viewType, byte[] viewStructure, byte[] _background) {
        this();
        this.setId(id);
        this.setName(name);
        this.setDescription(description);
        this.background = Utils.getImageFromByteArray(_background);
        this.setViewType(viewType);
        this.viewStructure = viewStructure;
    }

    public byte[] getViewStructure(){
        return viewStructure;
    }

    public Image getBackground() {
        return background;
    }

    public double[] getCenter() {
        return center;
    }

    public int getZoom(){
        return zoom;
    }

}
