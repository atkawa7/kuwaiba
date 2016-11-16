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
import com.vaadin.server.VaadinSession;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.events.ControlPointInfoWindowClosedListener;
import org.kuwaiba.custom.events.ControlPointMarkerClickListener;
import org.kuwaiba.custom.events.ControlPointMarkerDragListener;
import org.kuwaiba.custom.events.NodeMarkerClickListener;
import org.kuwaiba.custom.events.NodeMarkerDragListener;
import org.kuwaiba.custom.map.buttons.ConnectionButton;
import org.kuwaiba.custom.map.buttons.MarkerButton;
import org.kuwaiba.custom.map.buttons.MeasureButton;
import org.kuwaiba.custom.overlays.NodeMarker;

/**
 * GIS View section accept drop and select actions
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@SuppressWarnings("serial")
public class GISView extends CustomComponent {
    private final String apiKey = "";
    private final GoogleMap googleMap = new GoogleMap(apiKey, null, "english");
    private double centerLat = 2.4448;
    private double centerLon = -76.6147;  
    // mark
    // connection
    // measure
    private List<Connection> edges;
    
    public GISView(final EventBus eventBus, VaadinSession session) {   
        edges = new ArrayList();
        // initialize status of buttons
        session.setAttribute(MarkerButton.NAME, true);
        session.setAttribute(ConnectionButton.NAME, false);
        session.setAttribute(MeasureButton.NAME, false);
        
        googleMap.setCenter(new LatLon(centerLat, centerLon));
        googleMap.setZoom(17);
        googleMap.setWidth("1000px");
        googleMap.setHeight("500px");
        
        Connection connection = new Connection(googleMap);
        
        googleMap.addMarkerClickListener(new NodeMarkerClickListener(edges, connection, googleMap, session, eventBus));
        googleMap.addMarkerClickListener(new ControlPointMarkerClickListener(googleMap, session));
        
        ControlPointMarkerDragListener controlPointMarkerDragListener = new ControlPointMarkerDragListener(googleMap, session);

        googleMap.addMarkerDragListener(controlPointMarkerDragListener);
        googleMap.addMarkerDragListener(new NodeMarkerDragListener(connection, controlPointMarkerDragListener));
        
        googleMap.addInfoWindowClosedListener(new ControlPointInfoWindowClosedListener());
        
        DragAndDropWrapper wrapper = new DragAndDropWrapper(googleMap);
        
        wrapper.setSizeFull();        
        wrapper.setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                String strSource = event.getTransferable().getData(event.getTransferable().getDataFlavors().toArray()[0].toString()).toString();                
                //TODo: change this for more complex objects
                for (GoogleMapMarker marker : googleMap.getMarkers()) {
                    if (marker.getCaption().equals(strSource)) {
                        Notification.show("The object is in the map");
                        return;
                    }
                }
                NodeMarker nodeMarker = new NodeMarker(googleMap, strSource, googleMap.getCenter(), true);
                googleMap.addMarker(nodeMarker);
                googleMap.setInfoWindowContents(nodeMarker.getInfoWindow(), nodeMarker.getDeleteButton());
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        HorizontalLayout mapSearchLayout = new HorizontalLayout();
        
        TextField txtMapSearch = new TextField();
        mapSearchLayout.addComponent(txtMapSearch);
        
        Button btnMapSearch = new Button("Search");
        mapSearchLayout.addComponent(btnMapSearch);
        
        btnMapSearch.addClickListener(new MapSearch(txtMapSearch));
        
        VerticalLayout mapLayout = new VerticalLayout();
        mapLayout.setSizeFull();
        
        mapLayout.addComponent(mapSearchLayout);
        
        mapLayout.addComponent(wrapper);        
        
        HorizontalLayout hLayout = new HorizontalLayout();
        
        hLayout.setSizeFull();
        hLayout.addComponent(new MarkerButton(session, connection)); // remove connection
        hLayout.addComponent(new ConnectionButton(session));
        hLayout.addComponent(new MeasureButton(session));
                
        mapLayout.addComponent(hLayout);
        
        setCompositionRoot(mapLayout);
    } 
    
    private class MapSearch implements Button.ClickListener {
        private TextField txtMapSearch;        

        public MapSearch(TextField txtMapSearch) {
            this.txtMapSearch = txtMapSearch;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            for (GoogleMapMarker marker : googleMap.getMarkers())
                if (marker.getCaption().equals(txtMapSearch.getValue()))
                    googleMap.setCenter(marker.getPosition());
        }
    }
}
