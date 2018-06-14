package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import java.io.Serializable;

/**
 * Interface for listening polygon double click events.
 */
public interface PolygonDblClickListener extends Serializable {
    /**
     * Handle a PolygonDblClickEvent.
     *
     * @param clickedPolygon The polygon that was clicked.
     */
    void polygonDblClicked(GoogleMapPolygon clickedPolygon);
}
