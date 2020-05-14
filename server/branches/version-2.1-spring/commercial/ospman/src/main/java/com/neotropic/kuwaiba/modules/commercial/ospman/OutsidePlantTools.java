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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.shared.Registration;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;

/**
 * Component with a set of tools available to work in an outside plant canvas
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantTools extends HorizontalLayout {
    public enum Tool {
        HAND, MARKER, POLYGON, POLYLINE;
    }
    private Tool tool;
    
    public OutsidePlantTools(BusinessEntityManager bem) {
        tool = Tool.HAND;
        getElement().getStyle().set("background-color", "#fff");
        getElement().getStyle().set("left", "2%");
        getElement().getStyle().set("position", "absolute");
        getElement().getStyle().set("top", "8%");
        getElement().getStyle().set("z-index", "5");
        
        Button btnHand = new Button(new Icon(VaadinIcon.HAND));
        Button btnMarker = new Button(new Icon(VaadinIcon.MAP_MARKER));
        Button btnPolygon = new Button(new Icon(VaadinIcon.STAR_O));
        Button btnPolyline = new Button(new Icon(VaadinIcon.SPARK_LINE));

        btnHand.addClickListener(event -> {
            tool = Tool.HAND;
            enabledButtons(btnHand, btnMarker, btnPolygon, btnPolyline);
            fireEvent(new ToolChangeEvent(this, false));
        });
        btnMarker.addClickListener(event -> {
            tool = Tool.MARKER;
            enabledButtons(btnMarker, btnHand, btnPolygon, btnPolyline);
            fireEvent(new ToolChangeEvent(this, false));
        });
        btnPolygon.addClickListener(event -> {
            tool = Tool.POLYGON;
            enabledButtons(btnPolygon, btnHand, btnMarker, btnPolyline);
            fireEvent(new ToolChangeEvent(this, false));
        });
        btnPolyline.addClickListener(event -> {
            tool = Tool.POLYLINE;
            enabledButtons(btnPolyline, btnHand, btnMarker, btnPolygon);
            fireEvent(new ToolChangeEvent(this, false));
        });
        OutsidePlantSearch outsidePlantSearch = new OutsidePlantSearch(bem);
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        add(btnHand, btnMarker, btnPolygon, btnPolyline, outsidePlantSearch);
    }
    
    private void enabledButtons(Button disableButton, Button... enableButtons) {
        for (Button enableButton : enableButtons)
            enableButton.setEnabled(true);
        disableButton.setEnabled(false);
    }
    
    public Tool getTool() {
        return tool;
    }
    
    public Registration addToolChangeListener(ComponentEventListener<ToolChangeEvent> listener) {
        return addListener(ToolChangeEvent.class, listener);
    }
    
    public class ToolChangeEvent extends ComponentEvent<OutsidePlantTools> {
        private final Tool tool;
        
        public ToolChangeEvent(OutsidePlantTools source, boolean fromClient) {
            super(source, fromClient);
            tool = source.getTool();
        }
        
        public Tool getTool() {
            return tool;
        }
    }
}
