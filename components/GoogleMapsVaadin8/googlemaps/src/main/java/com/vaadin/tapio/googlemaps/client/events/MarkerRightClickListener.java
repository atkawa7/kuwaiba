package com.vaadin.tapio.googlemaps.client.events;

import java.io.Serializable;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;

/**
 * Interface for listening marker right-click events.
 */
public interface MarkerRightClickListener extends Serializable {
    /**
     * Handle a MarkerRightClickEvent.
     *
     * @param clickedMarker The marker that was clicked.
     */
    void markerRightClicked(GoogleMapMarker clickedMarker);
}
