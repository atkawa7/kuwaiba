/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.view;

import com.neotropic.kuwaiba.prototypes.nodes.EmployeeNode;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.CustomComponent;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Notification;

/**
 *
 * @author duckman
 */
@SuppressWarnings("serial")
public class GISView extends CustomComponent {
    public static int DEFAULT_ZOOM_LEVEL = 15;
    public static LatLon DEFAULT_CENTER_LOCATION = new LatLon(2.441916, -76.6063356);
    
    public GISView() {
        final GoogleMap map = new GoogleMap("", null, "english");
        map.setZoom(DEFAULT_ZOOM_LEVEL);
        map.setSizeFull();
        map.setCenter(DEFAULT_CENTER_LOCATION);
        
        map.addMarkerClickListener(new MarkerClickListener() {

            @Override
            public void markerClicked(GoogleMapMarker clickedMarker) {
                Notification.show(clickedMarker.getCaption(), Notification.Type.ERROR_MESSAGE);
            }
        });
        
        DragAndDropWrapper wrapper = new DragAndDropWrapper(map);
        wrapper.setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                Object transferable = event.getTransferable().getData("itemId");
                
                if (transferable instanceof EmployeeNode) {                
                    GoogleMapMarker mapMarker = new GoogleMapMarker(((EmployeeNode)transferable).toString(), 
                            map.getCenter(),true);
                    
                    map.addMarker(mapMarker);
                } else
                    Notification.show("Only employees are allowed to be dropped here", Notification.Type.ERROR_MESSAGE);
                
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        wrapper.setSizeFull();
        
        setCompositionRoot(wrapper);
        setSizeFull();
    }
    
    
    
}
