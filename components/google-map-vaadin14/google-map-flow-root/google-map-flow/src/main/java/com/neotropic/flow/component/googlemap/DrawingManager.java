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
package com.neotropic.flow.component.googlemap;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("drawing-manager")
@JsModule("./drawing-manager.js")
public class DrawingManager extends Component {
    
    public DrawingManager() {
    }
    public DrawingManager(OverlayType drawingMode, boolean drawingControl) {
        getElement().setProperty(Constants.Property.DRAWING_MODE, drawingMode.constantName());
        getElement().setProperty(Constants.Property.DRAWING_CONTROL, drawingControl);
    }
    public String getDrawingMode() {
        return getElement().getProperty(Constants.Property.DRAWING_MODE, null);
    }
    public void setDrawingMode(OverlayType drawingMode) {
        if (drawingMode != null)
            getElement().setProperty(Constants.Property.DRAWING_MODE, drawingMode.constantName());
        else
            getElement().setPropertyJson(Constants.Property.DRAWING_MODE, Json.createNull());
    }
    public boolean getDrawingControl() {
        return getElement().getProperty(Constants.Property.DRAWING_CONTROL, false);
    }
    public void setDrawingControl(boolean drawingControl) {
        getElement().setProperty(Constants.Property.DRAWING_CONTROL, drawingControl);
    }
    public Registration addDrawingManagerMarkerCompleteListener(
        ComponentEventListener<GoogleMapEvent.DrawingManagerMarkerCompleteEvent> listener) {
        return addListener(GoogleMapEvent.DrawingManagerMarkerCompleteEvent.class, listener);
    }
    public Registration addDrawingManagerPolylineCompleteListener(
        ComponentEventListener<GoogleMapEvent.DrawingManagerPolylineCompleteEvent> listener) {
        return addListener(GoogleMapEvent.DrawingManagerPolylineCompleteEvent.class, listener);
    }
    public Registration addDrawingManagerPolygonCompleteListener(
        ComponentEventListener<GoogleMapEvent.DrawingManagerPolygonCompleteEvent> listener) {
        return addListener(GoogleMapEvent.DrawingManagerPolygonCompleteEvent.class, listener);
    }
    public Registration addDrawingManagerRectangleCompleteListener(
        ComponentEventListener<GoogleMapEvent.DrawingManagerRectangleCompleteEvent> listener) {
        return addListener(GoogleMapEvent.DrawingManagerRectangleCompleteEvent.class, listener);
    }
}
