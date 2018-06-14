package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
/**
 * An RPC from client to server that is called when the user has finished drawing a edge in
 * Google Maps.
 */
public interface EdgeCompletedRpc extends ServerRpc {
    void edgeCompleted(GoogleMapPolyline edgeComplet);
}
