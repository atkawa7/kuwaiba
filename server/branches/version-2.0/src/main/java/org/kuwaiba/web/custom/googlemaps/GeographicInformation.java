 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.googlemaps;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.web.custom.wizards.connection.PopupConnectionWizardView;

/**
 * Show Geographic Information
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GeographicInformation extends CustomComponent {
    private VerticalLayout lytGeoInfo;
    private String apiKey = "AIzaSyDdSZZu-XWKVw1yoj81xJKrv9RNJsKL4WM";
    private double centerLat = 2.4448;
    private double centerLon = -76.6147;  
    private GoogleMap googleMap = new GoogleMap(apiKey, null, "english");
    private PopupConnectionWizardView wizard = new PopupConnectionWizardView();
    
    
    public GeographicInformation() {
        lytGeoInfo = new VerticalLayout();
        
        
        googleMap.setCenter(new LatLon(centerLat, centerLon));
        googleMap.setZoom(17);
        googleMap.setWidth("500px");
        googleMap.setHeight("300px");
        lytGeoInfo.addComponent(googleMap);
        lytGeoInfo.addComponent(wizard);
        
        setCompositionRoot(lytGeoInfo);
    }
}
