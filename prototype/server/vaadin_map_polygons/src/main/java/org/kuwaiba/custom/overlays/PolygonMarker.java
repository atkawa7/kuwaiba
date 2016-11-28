/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.custom.overlays;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;

/**
 *
 * @author johnyortega
 */
public class PolygonMarker extends GoogleMapMarker {
    public PolygonMarker() {
        setIconUrl("VAADIN/img/polygonControlPoint.png");
        setAnimationEnabled(false);
        setDraggable(true);
    }
}
