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
package org.kuwaiba.web.modules.osp.google.overlays;

import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import org.kuwaiba.web.custom.wizards.connection.PopupConnectionWizardView;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ConnectionPolyline extends GoogleMapPolyline {
    //private ObjectNode connection;
    private NodeMarker source;
    private NodeMarker target;
    private PopupConnectionWizardView wizard;
    
    public ConnectionPolyline(NodeMarker source) {
        this.source = source;
        getCoordinates().add(source.getPosition());
        target = null;
    }
    /*
    public ConnectionPolyline(NodeMarker source, NodeMarker target) {
        this.source = source;
        this.target = target;
        
        getCoordinates().add(source.getPosition());
        getCoordinates().add(target.getPosition());
    }
    */
    public NodeMarker getSource() {
        return source;
    }

    public void setSource(NodeMarker source) {
        this.source = source;
    }

    public NodeMarker getTarget() {
        return target;        
    }

    public void setTarget(NodeMarker target) {
        this.target = target;
        getCoordinates().add(target.getPosition());
    }

    public PopupConnectionWizardView getWizard() {
        return wizard;
    }

    public void setWizard(PopupConnectionWizardView wizard) {
        this.wizard = wizard;
    }
    
}
