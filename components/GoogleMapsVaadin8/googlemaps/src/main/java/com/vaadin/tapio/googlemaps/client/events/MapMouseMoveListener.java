package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.LatLon;
import java.io.Serializable;

/**
 * Interface for listening map mouse move events.
 */
public interface MapMouseMoveListener extends Serializable {
    /**
     * Handles a MapMouseMoveListener.
     *
     * @param position The position that was clicked.
     */
    void mapMouseMoved(LatLon position);
}
