package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;

/**
 * Interface for listening polygon right-click events.
 */
public interface PolygonRightClickListener {
    /**
     * Handle a PolygonRightClickEvent.
     *
     * @param clickedPolygon The polygon that was clicked.
     */
    void polygonRightClicked(GoogleMapPolygon clickedPolygon);
}
