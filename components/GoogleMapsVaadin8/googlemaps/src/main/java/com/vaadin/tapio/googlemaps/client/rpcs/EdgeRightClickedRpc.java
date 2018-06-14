package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;

/**
 * An RPC from client to server that is called when a edge has been right clicked in
 * Google Maps.
 */
public interface EdgeRightClickedRpc extends ServerRpc {
    void edgeRightClicked(GoogleMapPolyline rightClickedEdge);
}
