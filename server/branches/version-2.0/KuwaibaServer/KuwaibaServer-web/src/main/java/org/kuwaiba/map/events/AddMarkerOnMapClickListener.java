/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.map.events;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MapClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;

/**
 *
 * @author johnyortega
 */
public class AddMarkerOnMapClickListener implements MapClickListener {
    
    private static final long serialVersionUID = 1L;
    private final GoogleMap googleMap;
    
    
    public AddMarkerOnMapClickListener(GoogleMap map) {
        this.googleMap = map;
    }

    @Override
    public void mapClicked(LatLon position) {
        int id = googleMap.getMarkers().size();
        if (position != null)
            googleMap.addMarker(new GoogleMapMarker("New marker " + id, position, true, "VAADIN/img/building_32.png"));
        //googleMap.addMarker(new GoogleMapMarker("New marker " + id, position, true, null));
    }
    
}
