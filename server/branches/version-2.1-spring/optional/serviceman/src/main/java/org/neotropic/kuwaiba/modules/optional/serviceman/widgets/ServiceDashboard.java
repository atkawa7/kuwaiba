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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.BooleanProperty;
import org.neotropic.util.visual.properties.DateProperty;
import org.neotropic.util.visual.properties.DoubleProperty;
import org.neotropic.util.visual.properties.IntegerProperty;
import org.neotropic.util.visual.properties.ListTypeMultipleProperty;
import org.neotropic.util.visual.properties.ListTypeProperty;
import org.neotropic.util.visual.properties.LongProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.StringProperty;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceDashboard extends HorizontalLayout {
    /**
     * Reference to the service being displayed.
     */
    private BusinessObject service;
    private ClassMetadata classMetadata;
    /**
     * The property sheet with the attributes of the service.
     */
    private PropertySheet shtServiceProperties;
    
    public ServiceDashboard(BusinessObjectLight aService, TranslationService ts, 
            MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        setSizeFull();
        try {
            this.classMetadata = mem.getClass(aService.getClassName());
            this.service = bem.getObject(aService.getClassName(), aService.getId());
            List<AbstractProperty> serviceAttributes = new ArrayList<>();
            
            this.classMetadata.getAttributes().forEach(anAttribute -> {
                try {
                    switch (anAttribute.getType()) {
                        case Constants.DATA_TYPE_STRING:
                            serviceAttributes.add(new StringProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                    anAttribute.getDescription(), (String)this.service.getAttributes().get(anAttribute.getName())));
                            break;
                        case Constants.DATA_TYPE_FLOAT:
                            serviceAttributes.add(new DoubleProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                    anAttribute.getDescription(), (Double)this.service.getAttributes().get(anAttribute.getName())));
                            break;
                        case Constants.DATA_TYPE_INTEGER:
                            serviceAttributes.add(new IntegerProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                    anAttribute.getDescription(), (Integer)this.service.getAttributes().get(anAttribute.getName())));
                            break;
                        case Constants.DATA_TYPE_BOOLEAN:
                            serviceAttributes.add(new BooleanProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                    anAttribute.getDescription(), (Boolean)this.service.getAttributes().get(anAttribute.getName())));
                            break;
                        case Constants.DATA_TYPE_LONG:
                            serviceAttributes.add(new LongProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                    anAttribute.getDescription(), (Long)this.service.getAttributes().get(anAttribute.getName())));
                            break;
                        case Constants.DATA_TYPE_DATE:
                            serviceAttributes.add(new DateProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                    anAttribute.getDescription(), (Long)this.service.getAttributes().get(anAttribute.getName())));
                            break;
                        case Constants.DATA_TYPE_LIST_TYPE:
                            List<BusinessObjectLight> items = aem.getListTypeItems(anAttribute.getType());
                            if (anAttribute.isMultiple())
                                serviceAttributes.add(new ListTypeMultipleProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                        anAttribute.getDescription(), (List<BusinessObjectLight>)this.service.getAttributes().get(anAttribute.getName()), items));
                            else
                                serviceAttributes.add(new ListTypeProperty(anAttribute.getName(), anAttribute.getDisplayName(), 
                                        anAttribute.getDescription(), (BusinessObjectLight)this.service.getAttributes().get(anAttribute.getName()), items));
                            break;
                    }
                } catch(NumberFormatException ex) { // Faulty values will be ignored and silently logged
                    Logger.getLogger(ServiceDashboard.class.getName()).log(Level.SEVERE, 
                            String.format(ts.getTranslatedString(String.format("module.propertysheet.labels.wrong-data-type", anAttribute.getName(), 
                                    aService.getId(), this.service.getAttributes().get(anAttribute.getName())))));
                } catch(InventoryException ex) {
                }
            });
            
            this.shtServiceProperties = new PropertySheet(ts, serviceAttributes, ts.getTranslatedString("module.propertysheet.labels.header"));
            add(this.shtServiceProperties);
        } catch (InventoryException ex) {
            add(new Label(ex.getMessage()));
        }
    }
}
