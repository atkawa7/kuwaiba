package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import java.io.Serializable;

/**
 * Interface for listening polygon when the user has finished drawing a polygon.
 */
public interface PolygonCompleteListener extends Serializable {
    /**
     * Handle a PolygonCompleteEvent.
     *
     * @param completedPolygon The polygon that was completed.
     */ 
    void polygonCompleted(GoogleMapPolygon completedPolygon);
}
