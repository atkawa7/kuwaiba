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

import com.vaadin.tapio.googlemaps.client.events.PolylineClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.google.overlays.ConnectionPolyline;
import org.kuwaiba.web.modules.osp.google.overlays.Polyline;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolylineClickListenerImpl implements PolylineClickListener {
    
    @Override
    public void polylineClicked(GoogleMapPolyline clickedPolyline) {
        //TODO: replace this for right click and connect links
        if (clickedPolyline instanceof ConnectionPolyline) {
            RemoteObjectLight object = ((ConnectionPolyline) clickedPolyline).getConnectionInfo();
            ((Polyline) clickedPolyline)
                    .firePropertyChangeEvent("showConnectionLinksWindow", null, object);
        }
        if (clickedPolyline instanceof Polyline) {
            Polyline polyline = (Polyline) clickedPolyline;
            polyline.setEditable(!polyline.isEditable());
        }
    }
}
