/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.google.overlays;

import com.vaadin.tapio.googlemaps.client.LatLon;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MarkerPoint extends Marker {
    private boolean isPoint;
    /**
     * A special point is that is used
     * for represent an object position e.g.
     * in a Connection the source and target nodes, 
     * in a Polygon the end point that close the figure.
     */
    private boolean isSpecial = false;
        
    public MarkerPoint(LatLon position, boolean isPoint) {
        this.isPoint = isPoint;
        setAnimationEnabled(false);
        setDraggable(true);
        fixIconUrl(isPoint);
        setPosition(position);
    }
    
    private void fixIconUrl(boolean isPoint) {
        if (isPoint)
            setIconUrl("VAADIN/themes/nuqui/img/mod_icon_osp_point.png");
        else
            setIconUrl("VAADIN/themes/nuqui/img/mod_icon_osp_midpoint.png");
    }
    
    public boolean isPoint() {
        return isPoint;
    }
    
    public void setIsPoint(boolean isPoint) {
        this.isPoint = isPoint;
        fixIconUrl(isPoint);
    }
    
    public boolean isSpecial() {
        return isSpecial;
    }
    
    public void setIsSpecial(boolean isSpecial) {
        this.isSpecial = isSpecial;
    }
}
