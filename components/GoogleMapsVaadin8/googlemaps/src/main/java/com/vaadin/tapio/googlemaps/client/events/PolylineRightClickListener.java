package com.vaadin.tapio.googlemaps.client.events;

import java.io.Serializable;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;

/**
 * Interface for listening polyline right-click events.
 */
public interface PolylineRightClickListener extends Serializable {
    /**
     * Handle a PolyLineRightClickEvent.
     *
     * @param clickedPolyline The polyline that was clicked.
     */
    void polylineRightClicked(GoogleMapPolyline clickedPolyline);
}
