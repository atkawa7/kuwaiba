/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.web.modules.osp.dashboard;

import com.vaadin.server.ExternalResource;
import org.kuwaiba.web.modules.osp.OSPConstants;
import com.vaadin.server.Page;
import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A simple widget that shows a map and places all the buildings in the database with the attributes <code>longitude</code> and <code>latitude</code> set to valid values.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleMapDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    /**
     * Actual initial map longitude. See DEFAULT_XXX for default values.
     */
    private double mapLongitude;
    /**
     * Actual initial map latitude. See DEFAULT_XXX for default values.
     */
    private double mapLatitude;
    /**
     * Actual initial map zoom. See DEFAULT_XXX for default values.
     */
    private int mapZoom;
    
    
    public SimpleMapDashboardWidget(String title, WebserviceBean wsBean) {
        super(title);
        this.wsBean = wsBean;
        try {
            this.loadConfiguration();
            this.createContent();
        } catch (InvalidArgumentException ex) {
            addComponent(new Label(ex.getLocalizedMessage()));
        }
        this.setSizeFull();
    }
    
    public SimpleMapDashboardWidget(String title, WebserviceBean wsBean, long longitude, long latitude, int zoom) {
        super(title);
        this.wsBean= wsBean;
        this.mapLongitude = longitude;
        this.mapLatitude = latitude;
        this.mapZoom = zoom;
        this.createContent();
        this.setSizeFull();
    }

    @Override
    public void createContent() {
        String apiKey, language;
        try {
            apiKey = (String)wsBean.getConfigurationVariableValue("general.maps.apiKey", Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        } catch (ServerSideException ex) {
            apiKey = null;
            Notifications.showWarning("The configuration variable general.maps.apiKey has not been set. The default map will be used");
        }
        
        try {
            language = (String)wsBean.getConfigurationVariableValue("general.maps.language", Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        } catch (ServerSideException ex) {
            language = OSPConstants.DEFAULT_LANGUAGE;
        }

        GoogleMapsComponent<RemoteObjectLight, RemoteObjectLight> mapMain = new GoogleMapsComponent(apiKey, null, language);

        try {
            List<RemoteObjectLight> allPhysicalLocations = wsBean.getObjectsOfClassLight(Constants.CLASS_GENERICLOCATION, -1, Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            allPhysicalLocations.stream().forEach(aPhysicalLocation -> {
                    try {
                        String longitude = wsBean.getAttributeValueAsString(aPhysicalLocation.getClassName(), 
                                aPhysicalLocation.getId(), "longitude", Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                        if (longitude != null) {
                            String latitude = wsBean.getAttributeValueAsString(aPhysicalLocation.getClassName(), 
                                aPhysicalLocation.getId(), "latitude", Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                                        
                            if (latitude != null)
                                mapMain.addMarker(aPhysicalLocation, aPhysicalLocation.getName(), new LatLon(
                                    Float.valueOf(latitude), Float.valueOf(longitude)), false, 
                                    new ExternalResource("/img/default-map-marker.png").getURL());
                        }

                    } catch (ServerSideException ex) {
                        Notifications.showError(ex.getLocalizedMessage());
                    }
            });
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }

        mapMain.setSizeFull();
        mapMain.setCenter(new LatLon(mapLatitude, mapLongitude));
        mapMain.setZoom(mapZoom);
        //mapMain.showMarkerLabels(false);
        
        Panel pnlOptions = new Panel();
        pnlOptions.setWidth(100, Unit.PERCENTAGE);
        Button btnToggleLabels = new Button("Show Labels");
        //Since there is not a ToggleButton implementation, we will make our very simple own 
        btnToggleLabels.setData(false);
        btnToggleLabels.setWidth(100, Unit.PIXELS);
        btnToggleLabels.addClickListener((event) -> {
            if ((boolean)btnToggleLabels.getData()) {
                btnToggleLabels.setData(false);
                btnToggleLabels.setCaption("Show Labels");
                mapMain.showMarkerLabels(false);
            } else {
                btnToggleLabels.setData(true);
                btnToggleLabels.setCaption("Hide Labels");
                mapMain.showMarkerLabels(true);
            }
        });
        pnlOptions.setContent(btnToggleLabels);
        
        addComponents(/*pnlOptions,*/ mapMain);
        //setExpandRatio(pnlOptions, 3);
        //setExpandRatio(mapMain, 97);
    }

    
    @Override
    protected void loadConfiguration() throws InvalidArgumentException {
        try {
            this.mapLatitude = (double)wsBean.getConfigurationVariableValue("widgets.simplemap.centerLatitude", Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        } catch (ServerSideException | ClassCastException ex) {
            this.mapLatitude = OSPConstants.DEFAULT_CENTER_LATITUDE;
        }
        
        try {
            this.mapLongitude = (double)wsBean.getConfigurationVariableValue("widgets.simplemap.centerLongitude", Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        } catch (ServerSideException | ClassCastException ex) {
            this.mapLongitude = OSPConstants.DEFAULT_CENTER_LONGITUDE;
        }
        
        try {
            this.mapZoom = (int)wsBean.getConfigurationVariableValue("widgets.simplemap.zoom", Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        } catch (ServerSideException | ClassCastException ex) {
            this.mapZoom = OSPConstants.DEFAULT_ZOOM;
        }
    }
}
