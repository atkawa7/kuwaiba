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

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.events.PolygonRightClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.polygon.PolygonExt;
import org.kuwaiba.polygon.buttons.ButtonDeletePolygon;
import org.kuwaiba.polygon.buttons.ButtonEditPolygon;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolygonRightClickListenerImpl implements PolygonRightClickListener {
    private final GoogleMap googleMap;
    
    public PolygonRightClickListenerImpl(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public void polygonRightClicked(GoogleMapPolygon clickedPolygon) {
        if (clickedPolygon instanceof PolygonExt) {
            GoogleMapInfoWindow infoWindow = new GoogleMapInfoWindow();
            infoWindow.setPosition(clickedPolygon.getCoordinates().get(0));
            //TODO: set height and width don't work review the add-on
            //infoWindow.setHeight("2000 px");
            //infoWindow.setWidth("2000 px");
            PolygonExt polygonExt = (PolygonExt) clickedPolygon;
            polygonExt.setInfoWindow(infoWindow);
            
            VerticalLayout layout = new VerticalLayout();
            
            Button btnEdit = new ButtonEditPolygon((PolygonExt) polygonExt);
            Button btnDelete = new ButtonDeletePolygon((PolygonExt) polygonExt);
            
            /*
            Button btnDelete = new Button("Delete");
            btnDelete.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
                    */
            layout.addComponent(btnEdit);
            layout.addComponent(btnDelete);

            googleMap.setInfoWindowContents(infoWindow, layout);

            googleMap.openInfoWindow(infoWindow);
        }
    }
    
}
