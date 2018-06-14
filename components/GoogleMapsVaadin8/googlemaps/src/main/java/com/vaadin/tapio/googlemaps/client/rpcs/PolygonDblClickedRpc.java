package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;

/**
 * An RPC from client to server that is called when a polygon has been double-clicked in
 * Google Maps.
 */
public interface PolygonDblClickedRpc extends ServerRpc {
    void polygonDblClicked(long polygonId);
}
