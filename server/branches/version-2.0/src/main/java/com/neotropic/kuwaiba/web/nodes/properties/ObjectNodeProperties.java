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
package com.neotropic.kuwaiba.web.nodes.properties;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.neotropic.kuwaiba.web.nodes.ObjectNode;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.properties.PropertySheet;

/**
 * Represents a property sheet for an object node 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ObjectNodeProperties extends CustomComponent {
    
    private RemoteBusinessObject remoteBusinessObject;
    private final EventBus eventBus;
    private PropertySheet sheet;
    
    public ObjectNodeProperties(final EventBus eventBus) {
        this.eventBus = eventBus;
    }
      
    @Subscribe
    public void nodeSelected(ItemClickEvent event) {
        createPropertySheet((ObjectNode) event.getItemId());
    }
    
    private void createPropertySheet(ObjectNode node){
        try {
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
            ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
            
            remoteBusinessObject = bem.getObject(node.getClassName(), node.getId());
            sheet = new PropertySheet(remoteBusinessObject, eventBus);
            ClassMetadata meta = mem.getClass(remoteBusinessObject.getClassName());
            Set<AttributeMetadata> classAttributes = meta.getAttributes();
            int i = 0;
            for (AttributeMetadata classAttribute : classAttributes) {
                i++;
                if(classAttribute.isVisible()){
                    String attributeValue = "";
                    int mapping = AttributeMetadata.getMappingFromType(classAttribute.getType());
                    List<String> objectAttribute = remoteBusinessObject.getAttributes().get(classAttribute.getName());
                    
                    if (objectAttribute!=null)
                        attributeValue = objectAttribute.get(0);
                    
                    switch (mapping) {
                        case Constants.MAPPING_TIMESTAMP:
                        case Constants.MAPPING_DATE:
                            sheet.createDateProperty(classAttribute.getName(), new Date(Long.valueOf(attributeValue)), i);
                            break;
                        case Constants.MAPPING_PRIMITIVE:
                            sheet.createPrimitiveField(classAttribute.getName(), attributeValue, classAttribute.getType(), i);
                            break;
                        case Constants.MAPPING_MANYTOONE:
                            List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(classAttribute.getType());
                            RemoteBusinessObjectLight actualItem = null;
                            
                            for (RemoteBusinessObjectLight listTypeItem : listTypeItems) {
                                if(!attributeValue.isEmpty()){
                                    if(listTypeItem.getId() == Long.valueOf(attributeValue))
                                        actualItem = listTypeItem;
                                }
                            }
                            sheet.createListTypeField(classAttribute.getName(), listTypeItems, actualItem, i);
                            break;
//                        case Constants.MAPPING_MANYTOMANY:
//                            sheet.createTextField(classAttribute.getName(), "MtM", i);
//                            break;
                        default:
                           showNotification(new Notification("Mapping not supported",
                                   Notification.Type.ERROR_MESSAGE));
                    }        
                }
            }
            i++;
            sheet.createDefaults(i);
        } catch (InventoryException ex) {
            Logger.getLogger(ObjectNodeProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        setCompositionRoot(sheet);
    }
    
    private void showNotification(Notification notification) {
        // keep the notification visible a little while after moving the
        // mouse, or until clicked
        notification.setPosition(Position.BOTTOM_CENTER);
        notification.setDelayMsec(2000);
        notification.show(Page.getCurrent());
    }
}
