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
package org.kuwaiba.web.custom.osp;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.VaadinSession;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
//import org.kuwaiba.connection.Connection;
//import org.kuwaiba.connection.ConnectionUtils;
//import org.kuwaiba.custom.map.buttons.ConnectionButton;
//import org.kuwaiba.custom.map.buttons.MarkerButton;
//import org.kuwaiba.custom.overlays.ControlPointMarker;


/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NodeMarkerClickListener implements MarkerClickListener {

    private final EventBus eventBus;
    
    public NodeMarkerClickListener(final EventBus eventBus){
        this.eventBus = eventBus;
    }
            
    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        NodeMarkerOld m = (NodeMarkerOld)clickedMarker;
        eventBus.post(m);
    }
    
}

