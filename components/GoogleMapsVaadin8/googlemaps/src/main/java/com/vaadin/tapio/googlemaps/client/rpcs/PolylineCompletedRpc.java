package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
/**
 * An RPC from client to server that is called when the user has finished drawing a polyline in
 * Google Maps.
 */
public interface PolylineCompletedRpc extends ServerRpc {
    void polylineCompleted(GoogleMapPolyline gmPolyline);
}
