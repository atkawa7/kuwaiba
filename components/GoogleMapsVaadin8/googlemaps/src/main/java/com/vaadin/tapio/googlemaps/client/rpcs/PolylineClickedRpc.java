package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;

/**
 * An RPC from client to server that is called when a polyline has been clicked in
 * Google Maps.
 */
public interface PolylineClickedRpc extends ServerRpc {
    void polylineClicked(String polylineId);
}
