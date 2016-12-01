
package com.vaadin.tapio.googlemaps.client.rpcs;

import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.tapio.googlemaps.client.LatLon;

/**
 * An RPC from client to server that is called when a map has been right-clicked in
 * Google Maps.
 */
public interface MapRightClickedRpc extends ServerRpc {
    void mapRightClicked(LatLon position);
}
