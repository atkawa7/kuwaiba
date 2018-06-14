package com.vaadin.tapio.googlemaps.demo.events;

import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;

/**
 * Listener that opens info window when a marker is clicked.
 */
public class OpenInfoWindowOnMarkerClickListener implements MarkerClickListener {

    private static final long serialVersionUID = 646386541641L;

    private final GoogleMapsComponent map;
    private final GoogleMapMarker marker;
    private final GoogleMapInfoWindow window;

    public OpenInfoWindowOnMarkerClickListener(GoogleMapsComponent map,
            GoogleMapMarker marker, GoogleMapInfoWindow window) {
        this.map = map;
        this.marker = marker;
        this.window = window;
    }

    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        if (clickedMarker.equals(marker)) {
            map.openInfoWindow(window);
        }
    }

}
