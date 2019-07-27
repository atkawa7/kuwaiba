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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import java.awt.Color;
import java.awt.Dimension;

/**
 * Widget used to represent an empty rack unit which listen drop node actions 
 * when the user wants add an equipment in the rack view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackUnitWidget extends RackViewWidget {
    private static final Color COLOR_RACKUNIT = new Color(112, 112, 112);
    private final int rackUnitIndex;
    // Used to verify if a rack unit are in used
    private boolean available = true;
    
    public RackUnitWidget(RackViewScene scene, int rackUnitIndex, RackWidget parentRack) {
        super(scene);
        this.rackUnitIndex = rackUnitIndex;        
        setOpaque(true);
        setBackground(COLOR_RACKUNIT);                
        setMinimumSize(new Dimension(parentRack.getRackUnitWidth(), parentRack.getRackUnitHeight()));
    }
            
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {        
        this.available = available;
    }
        
    public int getRackUnitIndex() {
        return rackUnitIndex;
    }
    
}