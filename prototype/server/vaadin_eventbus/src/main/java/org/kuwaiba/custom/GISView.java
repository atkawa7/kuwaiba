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
package org.kuwaiba.custom;

import com.google.common.eventbus.EventBus;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.VerticalLayout;

/**
 * GIS View section accept drop and select actions
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@SuppressWarnings("serial")
public class GISView extends CustomComponent {
    private final String apiKey = "";
    private final GoogleMap googleMap = new GoogleMap(apiKey, null, "english");
    
    public GISView(final EventBus eventBus) {        
        googleMap.setCenter(new LatLon(60.440963, 22.25122));
        googleMap.setZoom(10);
        googleMap.setSizeFull();
        
        googleMap.addMarkerClickListener(new MarkerClickListener() {

            @Override
            public void markerClicked(GoogleMapMarker clickedMarker) {                
                eventBus.post(clickedMarker);
            }
        });
        
        DragAndDropWrapper wrapper = new DragAndDropWrapper(googleMap);
        wrapper.setSizeFull();
        
        wrapper.setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                String strSource = event.getTransferable().getData(event.getTransferable().getDataFlavors().toArray()[0].toString()).toString();
                GoogleMapMarker mapMarker = new GoogleMapMarker(strSource, new LatLon(60.44291, 22.242415),true, null);
                googleMap.addMarker(mapMarker);
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        
        VerticalLayout mapLayout = new VerticalLayout();
        mapLayout.setSizeFull();
        mapLayout.addComponent(wrapper);
        
        setCompositionRoot(mapLayout);
        setCompositionRoot(mapLayout);
    }    
}
