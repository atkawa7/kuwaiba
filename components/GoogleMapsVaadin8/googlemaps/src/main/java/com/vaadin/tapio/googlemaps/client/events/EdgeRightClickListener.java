package com.vaadin.tapio.googlemaps.client.events;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.io.Serializable;
/**
 * Interface for listening edge right click events.
 */
public interface EdgeRightClickListener extends Serializable {
    /**
     * Handle a EdgeRightClickEvent.
     *
     * @param rightClickedEdge The edge that was right clicked.
     */
    void edgeRightClicked(GoogleMapPolyline rightClickedEdge);    
}
