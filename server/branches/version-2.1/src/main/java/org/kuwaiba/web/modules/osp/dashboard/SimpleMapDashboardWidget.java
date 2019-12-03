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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import java.util.Properties;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.modules.osp.AbstractMapProvider;
import org.kuwaiba.web.modules.osp.GeoCoordinate;
import org.kuwaiba.web.modules.osp.OSPConstants;
import org.kuwaiba.web.modules.osp.google.GoogleMapsMapProvider;

/**
 * A simple widget that shows a map and places all the buildings in the database with the attributes <code>longitude</code> and <code>latitude</code> set to valid values.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleMapDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the backend bean
     */
    private WebserviceBean webserviceBean;
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
    private double mapZoom;
    
    
    public SimpleMapDashboardWidget(String title, WebserviceBean wsBean) {
        super(title);
        this.webserviceBean = wsBean;
        try {
            this.loadConfiguration();
            this.createContent();
        } catch (InvalidArgumentException ex) {
            add(new Label(ex.getLocalizedMessage()));
        }
        this.setSizeFull();
    }
    
    public SimpleMapDashboardWidget(String title, WebserviceBean wsBean, long longitude, long latitude, int zoom) {
        super(title);
        this.webserviceBean= wsBean;
        this.mapLongitude = longitude;
        this.mapLatitude = latitude;
        this.mapZoom = zoom;
        this.createContent();
        this.setSizeFull();
    }

    @Override
    public void createContent() {
        RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
        try {
            String className = (String) webserviceBean.getConfigurationVariableValue("general.maps.provider", 
                remoteSession.getIpAddress(), remoteSession.getSessionId()); //NOI18N
            try {
                Class mapsProviderClass = Class.forName(className);
                if (AbstractMapProvider.class.isAssignableFrom(mapsProviderClass)) {
                    try {
                        String apiKey = (String) webserviceBean.getConfigurationVariableValue("general.maps.apiKey", //NOI18N
                            remoteSession.getIpAddress(), remoteSession.getSessionId());
                        Properties mapProperties = new Properties();
                        mapProperties.put("apiKey", apiKey); //NOI18N
                        mapProperties.put("center", new GeoCoordinate(mapLatitude, mapLongitude)); //NOI18N
                        
                        if (GoogleMapsMapProvider.class.equals(mapsProviderClass)) {
                            GoogleMapsMapProvider mapProvider = new GoogleMapsMapProvider();
                            
                            setSizeFull();
                            mapProvider.initialize(mapProperties);
                            add(mapProvider.getComponent());
                        }

//                        AbstractMapProvider mapProvider = (AbstractMapProvider) mapsProviderClass.newInstance();
//                        setSizeFull();
//                        mapProvider.initialize(mapProperties);
//                        add(mapProvider.getComponent());

                    } catch (ServerSideException ex) {
                        Notifications.showWarning("The configuration variable general.maps.apiKey has not been set. The default map will be used");
                    }
                                                                                                                                            
                } else
                    add(new Label(String.format("Class %s is not a valid map provider", mapsProviderClass.getCanonicalName())));
            } catch (ClassNotFoundException /*| InstantiationException | IllegalAccessException*/ ex) {
                add(new Label(String.format("An unexpected error occurred while loading the Welcome view: %s", ex.getLocalizedMessage())));
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
    }

    
    @Override
    protected void loadConfiguration() throws InvalidArgumentException {
        RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
        try {
            this.mapLatitude = (double) webserviceBean.getConfigurationVariableValue("widgets.simplemap.centerLatitude", 
                remoteSession.getIpAddress(), remoteSession.getSessionId());
        } catch (ServerSideException | ClassCastException ex) {
            this.mapLatitude = OSPConstants.DEFAULT_CENTER_LATITUDE;
        }
        
        try {
            this.mapLongitude = (double) webserviceBean.getConfigurationVariableValue("widgets.simplemap.centerLongitude", 
                remoteSession.getIpAddress(), remoteSession.getSessionId());
        } catch (ServerSideException | ClassCastException ex) {
            this.mapLongitude = OSPConstants.DEFAULT_CENTER_LONGITUDE;
        }
        
        try {
            this.mapZoom = (double) webserviceBean.getConfigurationVariableValue("widgets.simplemap.zoom", 
                remoteSession.getIpAddress(), remoteSession.getSessionId());
        } catch (ServerSideException | ClassCastException ex) {
            this.mapZoom = OSPConstants.DEFAULT_ZOOM;
        }
    }
}
