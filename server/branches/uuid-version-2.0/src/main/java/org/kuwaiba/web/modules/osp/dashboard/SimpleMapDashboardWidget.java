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

import com.vaadin.server.Page;
import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
     * Default map center longitude. This value is used when the configuration variable <code>widgets.simplemap.centerLongitude</code> can not be found or it's not a number.
     */
    private static double DEFAULT_CENTER_LONGITUDE = 12.8260721;
    /**
     * Default map center latitude. This value is used when the configuration variable <code>widgets.simplemap.centerLatitude</code> can not be found or it's not a number.
     */
    private static double DEFAULT_CENTER_LATITUIDE = 11.8399727;
    /**
     * Default map center latitude. This value is used when the configuration variable <code>widgets.simplemap.centerLongitude</code> can not be found or it's not a number.
     */
    private static int DEFAULT_ZOOM = 3;
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
        this.wsBean= wsBean;
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
        this.mapLongitude = longitude;
        this.mapLatitude = latitude;
        this.mapZoom = zoom;
        this.wsBean= wsBean;
        this.createContent();
        this.setSizeFull();
    }

    @Override
    public void createContent() {
        try {
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey");
            String language = (String)context.lookup("java:comp/env/mapLanguage");


            GoogleMapsComponent mapMain = new GoogleMapsComponent(apiKey.trim().isEmpty() ? null : apiKey, null, language);

            mapMain.setSizeFull();
            mapMain.showMarkerLabels(true);

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
                                    mapMain.addMarker(aPhysicalLocation.toString(), new LatLon(
                                        Float.valueOf(latitude), Float.valueOf(longitude)), false, "/icons/" + aPhysicalLocation.getClassName() + ".png");
                            }

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                        }
                });
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }

            mapMain.setCenter(new LatLon(mapLatitude, mapLongitude));
            mapMain.setZoom(mapZoom);

            addComponent(mapMain);

        } catch (NamingException ex) {
            Notifications.showError(String.format("Map configuration could not be retrieved: %s", ex.getLocalizedMessage()));
        }
    }

    
    @Override
    protected void loadConfiguration() throws InvalidArgumentException {
        this.mapLatitude = DEFAULT_CENTER_LATITUIDE;
        this.mapLongitude = DEFAULT_CENTER_LONGITUDE;
        this.mapZoom = DEFAULT_ZOOM;
    }
}
