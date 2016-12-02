package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;

/**
 * An RPC from client to server that is called when a polygon has been right-clicked in
 * Google Maps.
 */
public interface PolygonRightClickedRpc extends ServerRpc {
    void polygonRightClicked(long polygonId);
}
