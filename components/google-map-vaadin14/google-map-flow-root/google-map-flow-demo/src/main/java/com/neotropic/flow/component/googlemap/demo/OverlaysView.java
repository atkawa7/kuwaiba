/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.flow.component.googlemap.demo;

import com.neotropic.flow.component.googlemap.GoogleMap;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value="overlay")
public class OverlaysView extends VerticalLayout {
    @Value("${google.maps.api-key}")
    private String apiKey;
    @Value("${google.maps.libraries}")
    private String libraries;
    
    @Override
    public void onAttach(AttachEvent attachEvent) {
        setSizeFull();
        setMargin(false);
        setPadding(false);
        
        GoogleMap gMap = new GoogleMap(apiKey, null, libraries);
        gMap.setDisableDefaultUi(true);
        Tabs tabs = new Tabs();
        tabs.getStyle().set("position", "absolute");
        tabs.getStyle().set("z-index", "5");
        tabs.getStyle().set("top", "10px");
        tabs.getStyle().set("left", "25%");
        tabs.getStyle().set("background-color", "#fff");
        
        Tab tabMxGraphOverlay = new Tab(new Icon(VaadinIcon.SQUARE_SHADOW));
        Tab tabGoogleMap = new Tab(new Icon(VaadinIcon.GLOBE));
        tabs.add(tabMxGraphOverlay, tabGoogleMap);
        tabs.addSelectedChangeListener(event -> {
            if (tabMxGraphOverlay.equals(event.getSelectedTab())) {
            }
            else if (tabGoogleMap.equals(event.getSelectedTab()))
                UI.getCurrent().navigate(MainView.class);
        });
        
        add(tabs);
        add(gMap);
    }
}
