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
package org.kuwaiba.custom.map.buttons;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.connection.MapFileReader;
import org.kuwaiba.custom.overlays.NodeMarker;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class UploadMapButton extends Button {
    public UploadMapButton(GoogleMap googleMap, List<Connection> edges) {
        super("Upload Map");
        
        addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                MapFileReader mfr = new MapFileReader();
                
                mfr.readMapFile(edges, googleMap);
                
                for (NodeMarker node : mfr.getNodes())
                    googleMap.addMarker(node);
                    
                for (Connection edge : edges) {
                    for (GoogleMapMarker controlPoint : edge.getControlPoints())
                        if (!googleMap.getMarkers().contains(controlPoint))
                            googleMap.addMarker(controlPoint);                    
                    googleMap.addPolyline(edge.getConnection());
                }
            }
        });
    }
    
}
