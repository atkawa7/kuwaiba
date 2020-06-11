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
package com.neotropic.kuwaiba.modules.commercial.ospman.widgets;

import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantView;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.views.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.views.ViewNodeIconGenerator;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * A simple widget that shows a map and places all the buildings in the database with 
 * the attributes <code>latitude</code> and <code>longitude</code> set to valid values.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleMapDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the resource factory
     */
    private final ResourceFactory resourceFactory;
    /**
     * Reference to the Physical Connection Service.
     */
    private final PhysicalConnectionsService physicalConnectionService;
    
    public SimpleMapDashboardWidget(
        ApplicationEntityManager aem, BusinessEntityManager bem, 
        MetadataEntityManager mem, PhysicalConnectionsService physicalConnectionService, TranslationService ts, 
        ResourceFactory resourceFactory) {
        
        super(mem, aem, bem, ts);
        this.resourceFactory = resourceFactory;
        this.physicalConnectionService = physicalConnectionService;
        setSizeFull();
        createContent();
    }

    @Override
    public void createContent() {
        try {
            OutsidePlantView outsidePlantView = new OutsidePlantView(mem, aem, bem, physicalConnectionService, ts, 
                new ViewNodeIconGenerator(resourceFactory), false, () -> {});
            outsidePlantView.buildEmptyView();
            
            List<BusinessObjectLight> allPhysicalLocation = bem.getObjectsOfClassLight(Constants.CLASS_GENERICLOCATION, -1);
            allPhysicalLocation.stream().forEach(aPhysicalLocation -> {
                try {
                    String lat = bem.getAttributeValueAsString(aPhysicalLocation.getClassName(), aPhysicalLocation.getId(), "latitude"); //NOI18N
                    if (lat != null) {
                    String lng = bem.getAttributeValueAsString(aPhysicalLocation.getClassName(), aPhysicalLocation.getId(), "longitude"); //NOI18N
                        if (lng != null) {
                            BusinessObjectViewNode aNode = new BusinessObjectViewNode(aPhysicalLocation);
                            aNode.getProperties().put("lat", Double.valueOf(lat)); //NOI18N
                            aNode.getProperties().put("lon", Double.valueOf(lng)); //NOI18N
                            outsidePlantView.getAsViewMap().addNode(aNode);
                        }
                    }
                                        
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                }
            });
            add(outsidePlantView.getAsComponent());
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()
            ).open();
        }
    }
}
