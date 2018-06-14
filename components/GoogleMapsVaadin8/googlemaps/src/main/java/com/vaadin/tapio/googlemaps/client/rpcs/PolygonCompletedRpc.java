package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
/**
 * An RPC from client to server that is called when the user has finished drawing a polygon in
 * Google Maps.
 */
public interface PolygonCompletedRpc extends ServerRpc {
    void polygonComplete(GoogleMapPolygon gmPolygon);
}
