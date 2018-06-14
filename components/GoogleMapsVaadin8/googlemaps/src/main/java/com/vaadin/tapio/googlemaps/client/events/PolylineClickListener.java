package com.vaadin.tapio.googlemaps.client.events;

import java.io.Serializable;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;

/**
 * Interface for listening polyline click events.
 */
public interface PolylineClickListener extends Serializable {
    /**
     * Handle a PolylineClickEvent.
     *
     * @param clickedPolyline The polyline that was clicked.
     */
    void polylineClicked(GoogleMapPolyline clickedPolyline);
}
