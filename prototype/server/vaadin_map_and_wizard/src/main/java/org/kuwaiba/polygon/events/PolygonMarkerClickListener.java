/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.polygon.events;

import com.vaadin.server.VaadinSession;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import org.kuwaiba.custom.map.buttons.DrawPolygonButton;
import org.kuwaiba.custom.overlays.PolygonMarker;

/**
 *
* @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolygonMarkerClickListener implements MarkerClickListener {
    private final VaadinSession session;
    private final PolygonMapClickListener mapClickListener;
            
    public PolygonMarkerClickListener(VaadinSession session, PolygonMapClickListener mapClickListener) {
        this.session = session;
        this.mapClickListener = mapClickListener;
    }

    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        if (clickedMarker instanceof PolygonMarker)
            if ((Boolean) session.getAttribute(DrawPolygonButton.NAME))
                mapClickListener.mapClicked(clickedMarker.getPosition());
    }    
}
