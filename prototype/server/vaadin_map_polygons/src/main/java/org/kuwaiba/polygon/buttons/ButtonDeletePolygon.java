/*
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
package org.kuwaiba.polygon.buttons;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.ui.Button;
import org.kuwaiba.custom.overlays.PolygonMarker;
import org.kuwaiba.polygon.MapPolygon;
import org.kuwaiba.polygon.PolygonExt;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ButtonDeletePolygon extends Button {
    private final PolygonExt polygonExt;
    
    public ButtonDeletePolygon(PolygonExt polygonExt) {
        super("Delete");
        this.polygonExt = polygonExt;
        addClickListener(new ButtonClickListenerImpl());
    }
    
    private class ButtonClickListenerImpl implements Button.ClickListener {
        public ButtonClickListenerImpl() {
        }

        @Override
        public void buttonClick(ClickEvent event) {
            MapPolygon mapPolygon = polygonExt.getMapPolygon();
            GoogleMap googleMap = mapPolygon.getGoogleMap();
            //TODO: close the info windows the impl don't work

            
            if (mapPolygon.getVertices() != null) {
                for (PolygonMarker vertex : mapPolygon.getVertices())
                    if (googleMap.getMarkers().contains(vertex))
                        googleMap.removeMarker(vertex);
            }
            googleMap.removePolygonOverlay(mapPolygon.getPolygon());
            
            if (mapPolygon.getPolyline() != null)
                googleMap.removePolyline(mapPolygon.getPolyline());
            if (mapPolygon.getDragListener() != null)
                googleMap.removeMarkerDragListener(mapPolygon.getDragListener());
            if (mapPolygon.getClickListener() != null)
                googleMap.removeMarkerClickListener(mapPolygon.getClickListener());
            
            if (googleMap.isInfoWindowOpen(polygonExt.getInfoWindow()))
                googleMap.closeInfoWindow(polygonExt.getInfoWindow());
            //mapPolygons.remove(mapPolygon);
        }
    }
}