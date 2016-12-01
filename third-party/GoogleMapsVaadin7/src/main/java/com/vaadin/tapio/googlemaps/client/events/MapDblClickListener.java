package com.vaadin.tapio.googlemaps.client.events;

import java.io.Serializable;

import com.vaadin.tapio.googlemaps.client.LatLon;

/**
 * Interface for listening map double-click events.
 */
public interface MapDblClickListener extends Serializable {
    /**
     * Handles a MapDblClickListener.
     *
     * @param position The position that was clicked.
     */
    void mapDblClicked(LatLon position);
}
