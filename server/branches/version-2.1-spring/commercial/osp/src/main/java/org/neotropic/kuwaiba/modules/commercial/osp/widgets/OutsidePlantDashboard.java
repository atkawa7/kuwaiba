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
package org.neotropic.kuwaiba.modules.commercial.osp.widgets;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Properties;
import org.neotropic.kuwaiba.modules.commercial.osp.AbstractMapProvider;
import org.neotropic.kuwaiba.modules.commercial.osp.GeoCoordinate;
import org.neotropic.kuwaiba.modules.commercial.osp.OutsidePlantConstants;
import org.neotropic.kuwaiba.core.apis.integration.AbstractDashboard;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.osp.google.GoogleMapMapProvider;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * The visual entry point to the Outside Plan Module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantDashboard extends VerticalLayout implements AbstractDashboard {
    /**
     * Reference to the translation service.
     */
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    
    private double longitude;
    private double latitude;
    private int zoom;
    
    public OutsidePlantDashboard(
        TranslationService ts, 
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        MetadataEntityManager mem) {
        this.ts = ts;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        setPadding(false);
        setMargin(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        try {
            this.latitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLatitude");
        } catch (InventoryException | ClassCastException ex) {
            this.latitude = OutsidePlantConstants.DEFAULT_CENTER_LATITUDE;
        }
        try {
            this.longitude = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLongitide");
        } catch (InventoryException | ClassCastException ex) {
            this.longitude = OutsidePlantConstants.DEFAULT_CENTER_LONGITUDE;
        }
        try {
            this.zoom = (int) aem.getConfigurationVariableValue("widgets.simplemap.zoom");
        } catch (InventoryException | ClassCastException ex) {
            this.zoom = OutsidePlantConstants.DEFAULT_ZOOM;
        }
        try {
            super.onAttach(attachEvent);
            setSizeFull();
            String className = "org.neotropic.kuwaiba.modules.commercial.osp.google.GoogleMapMapProvider";//(String) aem.getConfigurationVariableValue("general.maps.provider"); //NOI18N
            Class mapProviderClass = Class.forName(className);
            if (AbstractMapProvider.class.isAssignableFrom(mapProviderClass)) {
                String apiKey = (String) aem.getConfigurationVariableValue("general.maps.apiKey"); //NOI18N
                Properties mapProperties = new Properties();
                mapProperties.put("apiKey", apiKey); //NOI18N
                mapProperties.put("center", new GeoCoordinate(latitude, longitude)); //NOI18N
                mapProperties.put("zoom", zoom);
                
                setSizeFull();
                if (GoogleMapMapProvider.class.equals(mapProviderClass)) {
                    GoogleMapMapProvider mapProvider = new GoogleMapMapProvider();

                    setSizeFull();
                    mapProvider.initialize(mapProperties);
                    add(mapProvider.getComponent());
                }
//                AbstractMapProvider mapProvider = (AbstractMapProvider) mapProviderClass.newInstance();
//                mapProvider.initialize(mapProperties);
//                add(mapProvider.getComponent());
            }
        } catch (Exception ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()
            ).open();
        }
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS)
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
    
}
