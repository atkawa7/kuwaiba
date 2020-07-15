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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.function.Consumer;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("overlay-view")
@JsModule("./overlay-view.js")
public class OverlayView extends Component implements HasComponents {
    
    public OverlayView() {
    }
    
    public OverlayView(LatLngBounds bounds) {
        if (bounds != null)
            getElement().setPropertyJson(Constants.Property.BOUNDS, bounds.toJson());
    }
    
    public LatLngBounds getBounds() {
        return new LatLngBounds(
            (JsonObject) getElement().getPropertyRaw(Constants.Property.BOUNDS)
        );
    }
    
    public void setBounds(LatLngBounds bounds) {
        if (bounds != null)
            getElement().setPropertyJson(Constants.Property.BOUNDS, bounds.toJson());
        else
            getElement().setPropertyJson(Constants.Property.BOUNDS, Json.createNull());
    }
    
    public void fromLatLngToDivPixel(LatLng latLng, Consumer<Point> consumer) {
        getElement()
            .executeJs("return this.fromLatLngToDivPixel($0, $1)", latLng.getLat(), latLng.getLng())
            .then(JsonObject.class, point -> {
                consumer.accept(new Point(
                    point.getNumber(Constants.Property.X), 
                    point.getNumber(Constants.Property.Y)
                ));
            });
    }
    
    public Registration addWidthChangedListener(ComponentEventListener<GoogleMapEvent.OverlayViewWidthChangedEvent> listener) {
        return addListener(GoogleMapEvent.OverlayViewWidthChangedEvent.class, listener);
    }
}
