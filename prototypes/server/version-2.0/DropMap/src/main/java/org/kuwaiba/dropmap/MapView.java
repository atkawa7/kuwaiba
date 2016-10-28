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
package org.kuwaiba.dropmap;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;

/**
 * View to drag an node choose on the Tree to drag in the map
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MapView extends CustomComponent implements View  {
    public static String NAME = "mapView";
    
    private final String apiKey = null;
    private final GoogleMap googleMap = new GoogleMap(apiKey, null, "english");
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        // *** TREE AND NAME LABEL PANEL ***
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
                        
        Tree menu = new Tree();
        menu.addItem("Colombia");
        menu.addItem("Valle del Cauca");
        menu.addItem("Santiago de Cali");
        menu.addItem("Cauca");
        menu.addItem("Popayán");
        menu.addItem("Nariño");
        menu.addItem("Pasto");
        
        menu.setParent("Valle del Cauca", "Colombia");
        menu.setParent("Cauca", "Colombia");
        menu.setParent("Nariño", "Colombia");
        menu.setParent("Santiago de Cali", "Valle del Cauca");
        menu.setParent("Popayán", "Cauca");
        menu.setParent("Pasto", "Nariño");
        
        menu.expandItem("Colombia");
        menu.expandItem("Valle del Cauca");
        menu.expandItem("Cauca");
        menu.expandItem("Nariño");
        
        menu.setDragMode(TreeDragMode.NODE);
        
        content.addComponent(menu);
        // *** GOOGLE MAP PANEL ***
        googleMap.setCenter(new LatLon(60.440963, 22.25122));
        googleMap.setZoom(10);
        googleMap.setSizeFull();
        
        DragAndDropWrapper wrapper = new DragAndDropWrapper(googleMap);
        wrapper.setSizeFull();
        
        wrapper.setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                String strSource = event.getTransferable().getData(event.getTransferable().getDataFlavors().toArray()[0].toString()).toString();
                GoogleMapMarker mapMarker = new GoogleMapMarker(strSource, new LatLon(60.44291, 22.242415),true, null);
                googleMap.addMarker(mapMarker);
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        
        VerticalLayout mapLayout = new VerticalLayout();
        mapLayout.setSizeFull();
        mapLayout.addComponent(wrapper);
        // *** JOIN PANEL ***
        splitPanel.setFirstComponent(content);
        splitPanel.setSecondComponent(mapLayout);
        splitPanel.setSizeFull();
        // *** SET ROOT LAYOUT ***
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(splitPanel);
        layout.setSizeFull();
        
        this.setCompositionRoot(layout);
    }
}
