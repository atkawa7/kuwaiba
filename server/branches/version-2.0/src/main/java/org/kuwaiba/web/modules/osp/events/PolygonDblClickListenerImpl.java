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

import com.vaadin.tapio.googlemaps.client.events.PolygonDblClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import org.kuwaiba.web.modules.osp.google.overlays.Polygon;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolygonDblClickListenerImpl implements PolygonDblClickListener {

    @Override
    public void polygonDblClicked(GoogleMapPolygon clickedPolygon) {
        //TODO: replace this for right click and delete polygon
        /*
        if (clickedPolygon instanceof Polygon)
            ((Polygon) clickedPolygon)
                    .firePropertyChangeEvent("removePolygon", null, null);
                */
    }
    
}
