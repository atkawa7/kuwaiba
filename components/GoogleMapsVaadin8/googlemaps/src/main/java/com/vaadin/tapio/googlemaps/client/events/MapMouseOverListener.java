package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.LatLon;
import java.io.Serializable;
/**
 * Interface for listening map mouse over events.
 */
public interface MapMouseOverListener extends Serializable {
    /**
     * Handles a MapMouseOverListener.
     *
     * @param position The position that was clicked.
     */
    void mapMouseOver(LatLon position);
}
