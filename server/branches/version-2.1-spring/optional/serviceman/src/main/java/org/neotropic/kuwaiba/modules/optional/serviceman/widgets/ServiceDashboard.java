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

package org.neotropic.kuwaiba.modules.optional.serviceman.widgets;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyFactory;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceDashboard extends HorizontalLayout {
    /**
     * Reference to the service being displayed.
     */
    private BusinessObject service;
    /**
     * The property sheet with the attributes of the service.
     */
    private PropertySheet shtServiceProperties;
    
    public ServiceDashboard(BusinessObjectLight aService, TranslationService ts, 
            MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        setSizeFull();
        try {
            this.service = bem.getObject(aService.getClassName(), aService.getId());
            List<AbstractProperty> serviceAttributes = PropertyFactory.propertiesFromBusinessObject(this.service, ts, aem, mem);
            this.shtServiceProperties = new PropertySheet(ts, serviceAttributes, ts.getTranslatedString("module.propertysheet.labels.header"));
            add(this.shtServiceProperties);
            add(new NetworkResourcesWidget(ts.getTranslatedString("module.serviceman.widgets.network-resources.title")));
        } catch (InventoryException ex) {
            add(new Label(ex.getMessage()));
        }
    }
}
