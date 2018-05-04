/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.providers.google.overlays;

//import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.web.modules.osp.providers.google.actions.ActionsFactory;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Polygon /*extends GoogleMapPolygon*/ {
    public static final String DEFAULT_POLYGON_COLOR = "#AAD400";
    public static final double DEFAULT_FILL_OPACITY = 0.5;
    
    List<AbstractAction> actions;
    
    public Polygon() {
//        setStrokeColor(Polygon.DEFAULT_POLYGON_COLOR);
//        setFillColor(Polygon.DEFAULT_POLYGON_COLOR);
//        setFillOpacity(Polygon.DEFAULT_FILL_OPACITY);
    }
    
//    public Polygon(GoogleMapPolygon gmPolygon) {
//        this();
//        setId(gmPolygon.getId());
//        setCoordinates(gmPolygon.getCoordinates());
//    }
    
    public List<AbstractAction> getActions() {
        if (actions == null) {
            actions = new ArrayList();        
            actions.add(ActionsFactory.createDeletePolygonAction());
        }
        return actions;
    }
}
