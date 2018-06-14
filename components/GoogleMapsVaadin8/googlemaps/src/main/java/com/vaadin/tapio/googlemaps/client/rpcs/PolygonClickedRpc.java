package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;

/**
 * An RPC from client to server that is called when a polygon has been clicked in
 * Google Maps.
 */
public interface PolygonClickedRpc extends ServerRpc {
    void polygonClicked(GoogleMapPolygon gmPolygon);
}