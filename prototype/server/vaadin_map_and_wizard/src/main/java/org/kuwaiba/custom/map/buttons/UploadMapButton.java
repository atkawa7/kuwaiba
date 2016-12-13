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
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import com.vaadin.ui.NativeSelect;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.connection.ConnectionUtils;
import org.kuwaiba.custom.map.xml.MapFileReader;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;
import org.kuwaiba.polygon.MapPolygon;
import org.kuwaiba.utils.Constants;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class UploadMapButton extends Button {
    
    public UploadMapButton(GoogleMap googleMap, List<Connection> edges, List<MapPolygon> mapPolygons, NativeSelect nativeSelect) {
        super("Upload Map");
        
        addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                MapFileReader mfr = new MapFileReader();
                
                mfr.readMapFile(edges, mapPolygons, googleMap);
                
                for (NodeMarker node : mfr.getNodes())
                    googleMap.addMarker(node);
                    
                for (Connection edge : edges) {
                    List<ControlPointMarker> dummyControlPoints = new ArrayList();
                    
                    for (GoogleMapMarker controlPoint : edge.getControlPoints())
                        if (!googleMap.getMarkers().contains(controlPoint))
                            if (!controlPoint.getPosition().equals(edge.getSource().getPosition()) &&
                                    !controlPoint.getPosition().equals(edge.getTarget().getPosition())) {
                                
                                int index = edge.getControlPoints().indexOf(controlPoint);
                                int nextIndex = index + 1;
                                int size = edge.getControlPoints().size();
                                
                                googleMap.addMarker(controlPoint);
                                if (nextIndex != size) {
                                    ControlPointMarker nextControlPoint = edge.getControlPoints().get(nextIndex);
                                    LatLon latlon = ConnectionUtils.midPoint(controlPoint.getPosition(), nextControlPoint.getPosition());
                                    
                                    ControlPointMarker dummycp = new ControlPointMarker(latlon, edges);
                                    dummycp.setConnection(edge);
                                    dummycp.setIconUrl(Constants.dummyControlPointIconUrl);
                                    dummyControlPoints.add(dummycp);
                                    
                                    googleMap.addMarker(dummycp);
                                }
                                
                            }
                    ControlPointMarker srcControlPoint = edge.getControlPoints().get(0);
                    ControlPointMarker nextControlPoint = edge.getControlPoints().get(1);
                    LatLon latlon = ConnectionUtils.midPoint(srcControlPoint.getPosition(), nextControlPoint.getPosition());
                    ControlPointMarker dummycp = new ControlPointMarker(latlon, edges);
                    dummycp.setConnection(edge);
                    dummycp.setIconUrl(Constants.dummyControlPointIconUrl);
                    dummyControlPoints.add(0, dummycp);
                                    
                    googleMap.addMarker(dummycp);
                    
                    int size = dummyControlPoints.size();
                    int dummycpIdx = 1;
                    
                    for (int i = 0; i < size; i += 1) {
                        edge.getControlPoints().add(i + dummycpIdx, dummyControlPoints.get(i)); 
                        
                        dummycpIdx += 1;
                    }
                    
                    googleMap.addPolyline(edge.getEdge());
                }
                
                for (MapPolygon mapPolygon : mapPolygons) {
                    googleMap.addPolygonOverlay(mapPolygon.getPolygon());
                    nativeSelect.addItem(Long.toString(mapPolygon.getPolygonId()));
                }
            }
        });
    }
    
}
