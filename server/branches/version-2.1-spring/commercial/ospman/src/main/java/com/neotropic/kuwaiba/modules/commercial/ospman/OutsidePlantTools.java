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

import com.neotropic.flow.component.googlemap.GoogleMapMarker;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.google.DrawPolylineTool;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Component with a set of tools available to work in an outside plant canvas
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantTools extends HorizontalLayout {
    public enum Tool {
        HAND, MARKER, POLYGON, POLYLINE;
    }
    private Tool tool;
    private DrawPolylineTool drawPolylineTool;
    
    private final OutsidePlantSearch outsidePlantSearch;
    
    public OutsidePlantTools(BusinessEntityManager bem) {
        tool = Tool.HAND;
        getElement().getStyle().set("background-color", "#fff");
        getElement().getStyle().set("left", "2%");
        getElement().getStyle().set("position", "absolute");
        getElement().getStyle().set("top", "8%");
        getElement().getStyle().set("z-index", "5");
        
        Button btnHand = new Button(new Icon(VaadinIcon.HAND));
//        Button btnMarker = new Button(new Icon(VaadinIcon.MAP_MARKER));
        Button btnPolygon = new Button(new Icon(VaadinIcon.STAR_O));
        Button btnPolyline = new Button(new Icon(VaadinIcon.SPARK_LINE));

        btnHand.addClickListener(event -> {
            setDrawPolylineTool(null);
            tool = Tool.HAND;
            enabledButtons(btnHand, btnPolygon, btnPolyline);
            fireEvent(new ToolChangeEvent(this, false));
        });
//        btnMarker.addClickListener(event -> {
//            tool = Tool.MARKER;
//            enabledButtons(btnMarker, btnHand, btnPolygon, btnPolyline);
//            fireEvent(new ToolChangeEvent(this, false));
//        });
        btnPolygon.addClickListener(event -> {
            setDrawPolylineTool(null);
            tool = Tool.POLYGON;
            enabledButtons(btnPolygon, btnHand, btnPolyline);
            fireEvent(new ToolChangeEvent(this, false));
        });
        btnPolyline.addClickListener(event -> {
            tool = Tool.POLYLINE;
            enabledButtons(btnPolyline, btnHand, btnPolygon);
            fireEvent(new ToolChangeEvent(this, false));
        });
        outsidePlantSearch = new OutsidePlantSearch(bem);
        outsidePlantSearch.addNewListener(event -> {
            setDrawPolylineTool(null);
            tool = Tool.MARKER;
            enabledButtons(btnHand, btnPolygon, btnPolyline);
            fireEvent(new NewMarkerEvent(this, false, event.getObject()));
        });
////        outsidePlantSearch.addSelectionListener(event -> {
////            fireEvent(new CenterChangeEvent(this, false, event.getOspNode().getLocation()));
////        });
        outsidePlantSearch.addSelectionListener(event -> {
            fireEvent(new MarkerSelectedEvent(this, false, event.getOspNode()));
        });
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        add(btnHand, btnPolygon, btnPolyline, outsidePlantSearch);
    }
    public void setMarkers(List<OSPNode> markers) {
        outsidePlantSearch.setMarkers(markers);
    }
    public void setDrawPolylineTool(DrawPolylineTool drawPolylineTool) {
        if (this.drawPolylineTool != null)
            this.drawPolylineTool.unregisterListeners();
        this.drawPolylineTool = drawPolylineTool;
    }
    public void polylineCompleted(GoogleMapMarker source, GoogleMapMarker target, List<LatLng> path) {
        fireEvent(new PolylineCompletedEvent(this, false, source, target, path));
    }
    public Registration addPolylineCompletedListener(ComponentEventListener<PolylineCompletedEvent> listener) {
        return addListener(PolylineCompletedEvent.class, listener);
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
    
    public Registration addNewMarkerListener(ComponentEventListener<NewMarkerEvent> listener) {
        return addListener(NewMarkerEvent.class, listener);
    }
    
    public Registration addCenterChangeListener(ComponentEventListener<CenterChangeEvent> listener) {
        return addListener(CenterChangeEvent.class, listener);
    }
    
    public Registration addMarkerSelectedChangeListener(ComponentEventListener<MarkerSelectedEvent> listener) {
        return addListener(MarkerSelectedEvent.class, listener);
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
    public class NewMarkerEvent extends ComponentEvent<OutsidePlantTools> {
        private final BusinessObjectLight object;
        public NewMarkerEvent(OutsidePlantTools source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }
        public BusinessObjectLight getObject() {
            return object;
        }
    }
    public class CenterChangeEvent extends ComponentEvent<OutsidePlantTools> {
        private GeoCoordinate geoCoordinate;
        public CenterChangeEvent(OutsidePlantTools source, boolean fromClient, GeoCoordinate geoCoordinate) {
            super(source, fromClient);
            this.geoCoordinate = geoCoordinate;
        }
        public GeoCoordinate getGeoCoordinate() {
            return geoCoordinate;
        }
    }
    public class MarkerSelectedEvent extends ComponentEvent<OutsidePlantTools> {
        private final OSPNode ospNode;
        public MarkerSelectedEvent(OutsidePlantTools source, boolean fromClient, OSPNode ospNode) {
            super(source, fromClient);
            this.ospNode = ospNode;
        }
        public OSPNode getOspNode() {
            return ospNode;
        }
    }
    public class PolylineCompletedEvent extends ComponentEvent<OutsidePlantTools> {
        private final GoogleMapMarker sourceMarker;
        private final GoogleMapMarker targetMarker;
        private final List<LatLng> path;
        
        public PolylineCompletedEvent(OutsidePlantTools source, boolean fromClient, 
            GoogleMapMarker sourceMarker, GoogleMapMarker targetMarker, List<LatLng> path) {
            super(source, fromClient);
            this.sourceMarker = sourceMarker;
            this.targetMarker = targetMarker;
            this.path = path;
        }
        public GoogleMapMarker getSourceMarker() {
            return sourceMarker;
        }
        public GoogleMapMarker getTargetMarker() {
            return targetMarker;
        }
        public List<LatLng> getPath() {
            return path;
        }
    }
}
