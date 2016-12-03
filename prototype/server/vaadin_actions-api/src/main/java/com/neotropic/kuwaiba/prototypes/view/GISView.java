/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.view;

import com.neotropic.kuwaiba.prototypes.actions.AbstractAction;
import com.neotropic.kuwaiba.prototypes.nodes.EmployeeNode;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MapRightClickListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerRightClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolylineClickListener;
import com.vaadin.tapio.googlemaps.client.events.PolylineRightClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Notification;
import java.util.Arrays;

/**
 * The Map component, currently supporting only GoogleMaps as map provider
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@SuppressWarnings("serial")
public class GISView extends AbstractTooledComponent {
    /**
     * Default zoom level
     */
    public static int DEFAULT_ZOOM_LEVEL = 15;
    /**
     * Default center location, Popayán <3
     */
    public static LatLon DEFAULT_CENTER_LOCATION = new LatLon(2.441916, -76.6063356);
    /**
     * The map widget
     */
    final GoogleMap map;
    
    
    public GISView() {
        
        super(new AbstractAction[] {
                    new AbstractAction("Test Action for a Button") {
                        {
                            setIcon(new ThemeResource("icons/nav_tree_icon24.png"));
                        }
                        @Override
                        public void actionPerformed(Object sourceComponent, Object targetObject) {
                            Notification.show("You just clicked on a toolbar button!", Notification.Type.ERROR_MESSAGE);
                        }
                    }
                }, AbstractTooledComponent.TOOLBAR_ORIENTATION_HORIZONTAL, ToolBarSize.BIG);
        
        map = new GoogleMap("", null, "english");
        
        
        map.setZoom(DEFAULT_ZOOM_LEVEL);
        map.setSizeFull();
        map.setCenter(DEFAULT_CENTER_LOCATION);
        
        map.addMarkerClickListener(new MarkerClickListener() {

            @Override
            public void markerClicked(GoogleMapMarker clickedMarker) {
                Notification.show("Click on marker", Notification.Type.ERROR_MESSAGE);
            }
        });
        
        map.addMarkerRightClickListener(new MarkerRightClickListener() {

            @Override
            public void markerRightClicked(GoogleMapMarker clickedMarker) {
                Notification.show("Right click on marker", Notification.Type.ERROR_MESSAGE);
            }
        });
        
        
        GoogleMapPolyline polyline = map.addPolyline("Test Polyline");
        polyline.setCoordinates(Arrays.asList(new LatLon(2.441916, -76.6063356), new LatLon(2.441916, -76.607)));
        
        map.addPolylineClickListener(new PolylineClickListener() {

            @Override
            public void polylineClicked(GoogleMapPolyline clickedPolyline) {
                Notification.show("Click on polyline", Notification.Type.ERROR_MESSAGE);
            }
        });
        
        map.addPolylineRightClickListener(new PolylineRightClickListener() {

            @Override
            public void polylineRightClicked(GoogleMapPolyline clickedPolyline) {
                Notification.show("Right click on polyline", Notification.Type.ERROR_MESSAGE);
            }
        });
        
        map.addMapRightClickListener(new MapRightClickListener() {

            @Override
            public void mapRightClicked(LatLon position) {
                Notification.show("Right click on map", Notification.Type.ERROR_MESSAGE);
            }
        });
        
        DragAndDropWrapper wrapper = new DragAndDropWrapper(map);
        wrapper.setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                Object transferable = event.getTransferable().getData("itemId");
                
                if (transferable instanceof EmployeeNode) {
                    
                    map.addMarker(((EmployeeNode)transferable).toString(), 
                            map.getCenter(), true, null);
                    
                } else
                    Notification.show("Only employees are allowed to be dropped here", Notification.Type.ERROR_MESSAGE);
                
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        
        wrapper.setSizeFull();
        setMainComponent(wrapper);
        
    }
}
