package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;

/**
 * An RPC from client to server that is called when a polyline has been double-clicked in
 * Google Maps.
 */
public interface PolylineDblClickedRpc extends ServerRpc {
    void polylineDblClicked(String polylineId);
}
