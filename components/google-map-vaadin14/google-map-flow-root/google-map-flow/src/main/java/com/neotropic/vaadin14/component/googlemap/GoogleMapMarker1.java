/*
 * Copyright 2019 Johny Ortega.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neotropic.vaadin14.component.googlemap;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

/**
 *
 * @author johnyortega
 */
@Tag("google-map-marker")
@JsModule("./google-map-marker.js")
public class GoogleMapMarker1 extends Component {
//    private static final PropertyDescriptor<Double, Double> propertyLatitude = PropertyDescriptors.propertyWithDefault("latitude", (Double) null);
//    private static final PropertyDescriptor<Double, Double> propertyLongitude = PropertyDescriptors.propertyWithDefault("longitude", (Double) null);
//    private static final PropertyDescriptor<String, String> propertyLabel = PropertyDescriptors.propertyWithDefault("label", (String) null);
//    private static final PropertyDescriptor<Boolean, Boolean> propertyClickEvents = PropertyDescriptors.propertyWithDefault("clickEvents", Boolean.FALSE);
//    private static final PropertyDescriptor<Boolean, Boolean> propertyDragEvents = PropertyDescriptors.propertyWithDefault("dragEvents", Boolean.FALSE);
//    private static final PropertyDescriptor<Boolean, Boolean> propertyDraggable = PropertyDescriptors.propertyWithDefault("draggable", Boolean.FALSE);
//    private static final PropertyDescriptor<String, String> propertyTitle = PropertyDescriptors.propertyWithDefault("title", (String) null);
        
    public GoogleMapMarker1() {
         getElement().setProperty("latitude", "2.4573831");
         getElement().setProperty("longitude", "-76.6699746");

    }
        
    @Synchronize(property = "latitude", value = "latitude-changed")
    public double getLatitude() {
        return Double.valueOf(getElement().getProperty("latitude"));
    }
    
    public void setLatitude(double latitude) {
        getElement().setProperty("latitude", latitude);
    }
    
    @Synchronize(property = "longitude", value = "longitude-changed")
    public double getLongitude() {
        return Double.valueOf(getElement().getProperty("longitude"));
    }
    
    public void setLongitude(double longitude) {
        getElement().setProperty("longitude", longitude);
    }
            
    public String getLabel() {
        return getElement().getProperty("label");
    }
    
    public void setLabel(String label) {
        getElement().setProperty("label", label);
    }
    
    public boolean getClickEvents() {
        return Boolean.valueOf(getElement().getProperty("clickEvents"));
    }
    
    public void setClickEvents(boolean clickEvents) {
        getElement().setProperty("clickEvents", clickEvents);
    }
    
    public boolean getDragEvents() {
        return Boolean.valueOf(getElement().getProperty("dragEvents"));
    }
    
    public void setDragEvents(boolean dragEvents) {
        getElement().setProperty("dragEvents", dragEvents);
    }
    
    public boolean getDraggable() {
        return Boolean.valueOf(getElement().getProperty("draggable"));
    }
    
    public void setDraggable(boolean draggable) {
        getElement().setProperty("draggable", draggable);
    }
    
    public String getTitle() {
        return getElement().getProperty("title");
    }
    
    public void setTitle(String title) {
        getElement().setProperty("title", title);
    }
}
