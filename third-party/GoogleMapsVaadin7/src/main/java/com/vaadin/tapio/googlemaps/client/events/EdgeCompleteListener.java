
package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;

/**
 * Interface for listening edge when the user has finished drawing a edge.
 */
public interface EdgeCompleteListener {
    /**
     * Handle a EdgeCompleteEvent.
     *
     * @param completedEdge The edge that was completed.
     */    
    void edgeCompleted(GoogleMapPolyline completedEdge);
}
