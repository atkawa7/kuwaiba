package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import java.io.Serializable;

/**
 * Interface for listening polygon click events.
 */
public interface PolygonClickListener extends Serializable {
    /**
     * Handle a PolygonClickEvent.
     *
     * @param clickedPolygon The polygon that was clicked.
     */
    void polygonClicked(GoogleMapPolygon clickedPolygon);
}
