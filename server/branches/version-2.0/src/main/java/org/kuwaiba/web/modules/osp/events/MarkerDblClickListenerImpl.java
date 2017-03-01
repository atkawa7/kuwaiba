 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.events;

import com.vaadin.tapio.googlemaps.client.events.MarkerDblClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import org.kuwaiba.web.modules.osp.google.overlays.Marker;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerNode;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerPoint;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MarkerDblClickListenerImpl implements MarkerDblClickListener {

    @Override
    public void markerDblClicked(GoogleMapMarker clickedMarker) {
        //TODO: replace this for right click and delete marker
        /*
        if (clickedMarker instanceof MarkerNode)
            ((Marker) clickedMarker)
                    .firePropertyChangeEvent("removeMarker", null, clickedMarker);
        */
        if (clickedMarker instanceof MarkerPoint) {
            MarkerPoint point = (MarkerPoint) clickedMarker;
            
            if (point.isPoint())
                point.firePropertyChangeEvent("removeMarker", null, clickedMarker);
        }
    }
    
}
