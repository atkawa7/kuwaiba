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
package org.kuwaiba.web.custom.osp.google.overlays;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;

/**
 * Custom GoogleMapMarker for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NodeMarker extends GoogleMapMarker {
    //private ObjectNode node;    
    private final RemoteBusinessObjectLight remoteBusinessObject;
    
    public NodeMarker(RemoteBusinessObjectLight remoteBusinessObject) {
        this.remoteBusinessObject = remoteBusinessObject;
        setDraggable(true);
    }
    
    public RemoteBusinessObjectLight getRemoteBusinessObject() {
        return remoteBusinessObject;
    }
}
