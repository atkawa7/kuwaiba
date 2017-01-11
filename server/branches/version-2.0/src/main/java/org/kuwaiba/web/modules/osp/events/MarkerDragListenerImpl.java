/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.modules.osp.events;

import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import org.kuwaiba.web.modules.osp.google.overlays.NodeMarker;
import org.kuwaiba.web.modules.osp.google.overlays.PointMarker;

/**
 *
 * @author johnyortega
 */
public class MarkerDragListenerImpl implements MarkerDragListener {

    @Override
    public void markerDragged(GoogleMapMarker draggedMarker, LatLon oldPosition) {
        if (draggedMarker instanceof NodeMarker) {
            NodeMarker nodeMarker = (NodeMarker) draggedMarker;
            
            nodeMarker.firePropertyChangeEvent("position", oldPosition, nodeMarker.getPosition());
        }
        if (draggedMarker instanceof PointMarker) {
            PointMarker pointMarker = (PointMarker) draggedMarker;
            
            pointMarker.firePropertyChangeEvent("position", oldPosition, pointMarker.getPosition());
        }
    }
    
}
