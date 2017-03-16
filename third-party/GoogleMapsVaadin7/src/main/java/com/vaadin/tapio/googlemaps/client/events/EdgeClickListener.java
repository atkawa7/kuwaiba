package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.io.Serializable;

/**
 * Interface for listening edge click events.
 */
public interface EdgeClickListener extends Serializable {
    /**
     * Handle a EdgeClickEvent.
     *
     * @param clickedEdge The edge that was clicked.
     */
    void edgeClicked(GoogleMapPolyline clickedEdge); 
}
