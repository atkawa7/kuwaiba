package com.vaadin.tapio.googlemaps.client.events;

import java.io.Serializable;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;

/**
 * Interface for listening polyline double click events.
 */
public interface PolylineDblClickListener extends Serializable {
    /**
     * Handle a PolylineDblClickEvent.
     *
     * @param clickedPolyline The polyline that was clicked.
     */
    void polylineDblClicked(GoogleMapPolyline clickedPolyline);
}
