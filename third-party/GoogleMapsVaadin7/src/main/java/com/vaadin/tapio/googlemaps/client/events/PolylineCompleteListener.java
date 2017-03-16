package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.io.Serializable;

/**
 * Interface for listening polyline when the user has finished drawing a polyline.
 */
public interface PolylineCompleteListener extends Serializable {
    /**
     * Handle a PolylineCompleteEvent.
     *
     * @param completedPolyline The polyline that was completed.
     */    
    void polylineCompleted(GoogleMapPolyline completedPolyline);
}
