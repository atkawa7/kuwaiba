package com.vaadin.tapio.googlemaps.client.events;

import java.io.Serializable;

import com.vaadin.tapio.googlemaps.client.LatLon;

/**
 * Interface for listening map right-click events.
 */
public interface MapRightClickListener extends Serializable {
    /**
     * Handles a MapRightClickListener.
     *
     * @param position The position that was clicked.
     */
    void mapRightClicked(LatLon position);
}
