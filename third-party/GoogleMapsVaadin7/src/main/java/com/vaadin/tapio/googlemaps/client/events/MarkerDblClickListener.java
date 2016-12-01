package com.vaadin.tapio.googlemaps.client.events;

import java.io.Serializable;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;

/**
 * Interface for listening marker double-click events.
 */
public interface MarkerDblClickListener extends Serializable {
    /**
     * Handle a MarkerDblClickEvent.
     *
     * @param clickedMarker The marker that was clicked.
     */
    void markerDblClicked(GoogleMapMarker clickedMarker);
}
