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
package org.kuwaiba.apis.web.gui.nodes.properties;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CustomComponent;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.web.modules.osp.google.overlays.NodeMarker;

/**
 * This class contains the method that listens when a node is selected in the 
 * tree or a marker is selected in the map and creates a property sheet 
 * for the selected object. 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ObjectNodeProperty extends CustomComponent implements AbstractNodeProperty {
    
    private RemoteBusinessObject remoteBusinessObject;
    private final EventBus eventBus;
    private PropertySheet sheet;
    
    public ObjectNodeProperty(final EventBus eventBus) {
        this.eventBus = eventBus;
    }
      
    @Subscribe
    @Override
    public void nodeSelected(ItemClickEvent event) {
        createPropertySheet((RemoteBusinessObjectLight) ((InventoryObjectNode)event.getItemId()).getObject());
    }
    
    /** 
     * A marker is selected in GIS View
     * @param marker The selected marker
     */
    @Subscribe
    @Override
    public void markerSelected(NodeMarker marker) {
        createPropertySheet(marker.getRemoteBusinessObject());
    }
    
    /**
     * Registers this component in the event bus.
     */
    public void register() {
        eventBus.register(this);
    }
    
    /**
     * Unregisters this component from the event bus.
     */
    public void unregister() {
        eventBus.unregister(this);
    }
    
    @Override
    public void createPropertySheet(Object node){
        try {
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
            ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
            
            remoteBusinessObject = bem.getObject(((RemoteBusinessObjectLight)node).getClassName(), ((RemoteBusinessObjectLight)node).getId());
            BeanItem<RemoteBusinessObject> beanItem = new BeanItem<> (remoteBusinessObject);
            ObjectNodePropertyChangeValueListener valueListener = new ObjectNodePropertyChangeValueListener(beanItem, eventBus);
            sheet = new PropertySheet(beanItem, valueListener);
            
            ClassMetadata meta = mem.getClass(remoteBusinessObject.getClassName());
            Set<AttributeMetadata> classAttributes = meta.getAttributes();
            int i = 0;
            for (AttributeMetadata classAttribute : classAttributes) {
                i++;
                if(classAttribute.isVisible()){
                    String attributeValue = "";
                    
                    List<String> objectAttribute = remoteBusinessObject.getAttributes().get(classAttribute.getName());
                    
                    if (objectAttribute != null)
                        attributeValue = objectAttribute.get(0);
                    
                    switch (classAttribute.getMapping()) {
                        case AttributeMetadata.MAPPING_TIMESTAMP:
                        case AttributeMetadata.MAPPING_DATE:
                            sheet.createDateProperty(classAttribute.getName(), classAttribute.getDescription(), new Date(Long.valueOf(attributeValue)), i);
                            break;
                        case AttributeMetadata.MAPPING_PRIMITIVE:
                            sheet.createPrimitiveField(classAttribute.getName(),  classAttribute.getDescription(), attributeValue, classAttribute.getType(), i);
                            break;
                        case AttributeMetadata.MAPPING_MANYTOONE:
                            List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(classAttribute.getType());
                            RemoteBusinessObjectLight actualItem = null;
                            
                            for (RemoteBusinessObjectLight listTypeItem : listTypeItems) {
                                if(!attributeValue.isEmpty()){
                                    if(listTypeItem.getId() == Long.valueOf(attributeValue))
                                        actualItem = listTypeItem;
                                }
                            }
                            sheet.createListTypeField(classAttribute.getName(),  classAttribute.getDescription(), listTypeItems, actualItem, i);
                            break;
                        default:
                            NotificationsUtil.showError("Mapping not supported");
                    }        
                }
            }
        } catch (InventoryException ex) {
            NotificationsUtil.showError(ex.getLocalizedMessage());
        }
        setCompositionRoot(sheet);
    }
}
