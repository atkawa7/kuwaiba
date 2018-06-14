package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.tapio.googlemaps.client.LatLon;

/**
 * An RPC from client to server that is called when a map has mouse moved in
 * Google Maps.
 */
public interface MapMouseMovedRpc extends ServerRpc {
    void mapMouseMoved(LatLon position);
}
