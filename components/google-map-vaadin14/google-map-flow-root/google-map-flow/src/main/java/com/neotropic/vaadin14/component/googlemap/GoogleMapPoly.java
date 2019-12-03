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
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import java.util.List;

/**
 *
 * @author johnyortega
 */
@Tag("google-map-poly")
@JsModule("./google-map-poly.js")
public class GoogleMapPoly extends Component {
    private static final PropertyDescriptor<Boolean, Boolean> propertyClickable = PropertyDescriptors.propertyWithDefault("clickable", Boolean.FALSE);
    private static final PropertyDescriptor<Boolean, Boolean> propertyClickEvents = PropertyDescriptors.propertyWithDefault("clickEvents", Boolean.FALSE);
    private static final PropertyDescriptor<Boolean, Boolean> propertyClosed = PropertyDescriptors.propertyWithDefault("closed", Boolean.FALSE);
    private static final PropertyDescriptor<Boolean, Boolean> propertyDraggable = PropertyDescriptors.propertyWithDefault("draggable", Boolean.FALSE);
    private static final PropertyDescriptor<Boolean, Boolean> propertyDragEvents = PropertyDescriptors.propertyWithDefault("dragEvents", Boolean.FALSE);
    private static final PropertyDescriptor<Boolean, Boolean> propertyEditable = PropertyDescriptors.propertyWithDefault("editable", Boolean.FALSE);
    private static final PropertyDescriptor<String, String> propertyFillColor = PropertyDescriptors.propertyWithDefault("fillColor", (String) null);
    private static final PropertyDescriptor<Double, Double> propertyFillOpacity = PropertyDescriptors.propertyWithDefault("fillOpacity", (Double) null);
    private static final PropertyDescriptor<String, String> propertyStrokeColor = PropertyDescriptors.propertyWithDefault("strokeColor", "black");
    private static final PropertyDescriptor<Double, Double> propertyStrokeOpacity = PropertyDescriptors.propertyWithDefault("strokeOpacity", (Double) null);
    private static final PropertyDescriptor<Double, Double> propertyStrokeWeight = PropertyDescriptors.propertyWithDefault("strokeWeight", (Double) null);
    
    private List<GoogleMapPoint> path;
    
    public GoogleMapPoly() {
    }
    
    public List<GoogleMapPoint> getPath() {
        return path;        
    }
    
    public void setPath(List<GoogleMapPoint> path) {
        this.path = path;
        getElement().removeAllChildren();
        if (path != null && path.size() < 2)
            return;
        for (GoogleMapPoint point : path)
            getElement().appendChild(point.getElement());
    }
    
    public boolean getClickable() {
        return propertyClickable.get(this);
    }
    
    public void setClickable(boolean clickable) {
        propertyClickable.set(this, clickable);
    }
    
    public boolean getClickEvents() {
        return propertyClickEvents.get(this);
    }
    
    public void setClickEvents(boolean clickEvents) {
        propertyClickEvents.set(this, clickEvents);
    }
    
    public boolean getClosed() {
        return propertyClosed.get(this);
    }
    
    public void setClosed(boolean closed) {
        propertyClosed.set(this, closed);
    }
    
    public boolean getDraggable() {
        return propertyDraggable.get(this);
    }
    
    public void setDraggable(boolean draggable) {
        propertyDraggable.set(this, draggable);
    }
    
    public boolean getDragEvents() {
        return propertyDragEvents.get(this);
    }
    
    public void setDragEvents(boolean dragEvents) {
        propertyDragEvents.set(this, dragEvents);
    }
    
    public boolean getEditable() {
        return propertyEditable.get(this);
    }
    
    public void setEditable(boolean editable) {
        propertyEditable.set(this, editable);
    }
    
    public String getFillColor() {
        return propertyFillColor.get(this);
    }
    
    public void setFillColor(String fillColor) {
        propertyFillColor.set(this, fillColor);
    }
    
    public double getFillOpacity() {
        return propertyFillOpacity.get(this);
    }
    
    public void setFillOpacity(double fillOpacity) {
        propertyFillOpacity.set(this, fillOpacity);
    }
    
    public String getStrokeColor() {
        return propertyStrokeColor.get(this);
    }
    
    public void setStrokeColor(String strokeColor) {
        propertyStrokeColor.set(this, strokeColor);
    }
    
    public double getStrokeOpacity() {
        return propertyStrokeOpacity.get(this);
    }
    
    public void setStrokeOpacity(double strokeOpacity) {
        propertyStrokeOpacity.set(this, strokeOpacity);
    }
    
    public double getStrokeWeight() {
        return propertyStrokeOpacity.get(this);
    }
    
    public void setStrokeWeight(double strokeWeight) {
        propertyStrokeWeight.set(this, strokeWeight);
    }
    

}
