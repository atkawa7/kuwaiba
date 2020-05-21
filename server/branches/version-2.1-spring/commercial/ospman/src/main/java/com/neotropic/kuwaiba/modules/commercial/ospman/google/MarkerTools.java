/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.google;

import com.neotropic.flow.component.googlemap.GoogleMapMarker;
import com.neotropic.flow.component.googlemap.InfoWindow;
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPNode;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

/**
 * Component with a set of tools to markers: like delete
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MarkerTools extends VerticalLayout {
    public MarkerTools(OSPNode ospNode, GoogleMapMarker googleMapMarker, InfoWindow infoWindow) {
        HorizontalLayout hlyTools = new HorizontalLayout();
        
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), event -> {
            infoWindow.close();
            fireEvent(new DeleteMarkerEvent(this, false));
        });
        hlyTools.add(btnDelete);
        
        add(hlyTools);
    }
    public Registration addDeleteMarkerEventListener(
        ComponentEventListener<DeleteMarkerEvent> listener) {
        return addListener(DeleteMarkerEvent.class, listener);
    }
    public class DeleteMarkerEvent extends ComponentEvent<MarkerTools> {

        public DeleteMarkerEvent(MarkerTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
