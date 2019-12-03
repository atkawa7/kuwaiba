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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;

/**
 *
 * @author johnyortega
 */
@Tag("google-map-point")
@JsModule("./google-map-point.js")
public class GoogleMapPoint extends Component {
        
    public GoogleMapPoint(double latitude, double longitude) {
        getElement().setProperty("latitude", latitude);
        getElement().setProperty("longitude", longitude);
    }
    
    public double getLatitude() {
        return Double.valueOf(getElement().getProperty("latitude"));
    }
    
    public void setLatitude(double latitude) {
        getElement().setProperty("latitude", latitude);
    }
    
    public double getLongitude() {
        return Double.valueOf(getElement().getProperty("longitude"));
    }
    
    public void setLongitude(double longitude) {
        getElement().setProperty("longitude", longitude);
    }
}
