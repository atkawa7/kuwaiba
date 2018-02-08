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
package org.inventory.navigation.navigationtree.nodes;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * An node to wrapper an LocalObjectLight use to create a property sheet with 
 * properties set for read only
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ReadOnlyObjectNode extends AbstractNode {
    LocalObjectLight lol;
    CommunicationsStub com;

    public ReadOnlyObjectNode(LocalObjectLight lol) {
        super(Children.LEAF);
        this.lol = lol;
        com = CommunicationsStub.getInstance();
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        
        if (lol.getClassName() == null)
            return sheet;

        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        Sheet.Set mandatoryPropertySet = Sheet.createPropertiesSet(); //Set with the mandatory attributes

        LocalClassMetadata meta = com.getMetaForClass(lol.getClassName(), false);
        if (meta == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return sheet;
        }

        final LocalObject lo = com.getObjectInfo(lol.getClassName(), lol.getOid());

        if (lo == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return sheet;
        }

        for (final LocalAttributeMetadata lam : meta.getAttributes()) {
            if (lam.isVisible()) {
                PropertySupport.ReadOnly property = null;
                int mapping = lam.getMapping();
                switch (mapping) {
                    case Constants.MAPPING_TIMESTAMP:
                    case Constants.MAPPING_DATE:
                        property = new PropertySupport.ReadOnly(lam.getName(), Date.class, lam.getDisplayName(), lam.getDescription()) {

                            @Override
                            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                return (Date) lo.getAttribute(lam.getName());
                            }

                            @Override
                            public PropertyEditor getPropertyEditor(){
                                return new PropertyEditorSupport() {

                                    @Override
                                    public boolean supportsCustomEditor(){
                                        return false;
                                    }                                    
                                };
                            }
                        };
                        break;
                    case Constants.MAPPING_PRIMITIVE:                        
                    //Those attributes that are not multiple, but reference another object
                    //like endpointX in physicalConnections should be ignored, at least by now
                        if (!lam.getType().equals(LocalObjectLight.class)) {
                            property = new PropertySupport.ReadOnly(lam.getName(), String.class, lam.getDisplayName(), lam.getDescription()) {

                                @Override
                                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                    Object value = lo.getAttribute(lam.getName());
                                    if (value == null && getValueType() == Boolean.class)
                                        return false;
                                    return value;
                                }                            

                                @Override
                                public PropertyEditor getPropertyEditor(){
                                return new PropertyEditorSupport() {

                                    @Override
                                    public boolean supportsCustomEditor(){
                                        return false;
                                    }                                    
                                };
                                }
                            };
                        }
                        break;
                    case Constants.MAPPING_MANYTOONE:
                        //If so, this can be a reference to an object list item or a 1:1 to any other RootObject subclass
                        final List<LocalObjectListItem> list = com.getList(lam.getListAttributeClassName(), true, false);
                        if (list == null) {
                            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                            return sheet;
                        }

                        property = new PropertySupport.ReadOnly(lam.getName(), String.class, lam.getDisplayName(), lam.getDescription()) {

                            @Override
                            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                LocalObjectListItem val = null;
                                if (lo.getAttribute(lam.getName()) == null) 
                                    val = list.get(0); //None
                                else {
                                    for (LocalObjectListItem loli : list) {
                                        if (lo.getAttribute(lam.getName()).equals(loli)) {
                                            val = loli;
                                            break;
                                        }
                                    }
                                }
                                return val;
                            }

                            @Override
                            public PropertyEditor getPropertyEditor(){
                                return new PropertyEditorSupport() {

                                    @Override
                                    public boolean supportsCustomEditor(){
                                        return false;
                                    }                                    
                                };
                            }
                        };
                        break;
                    default:
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Mapping not supported");
                        return sheet;
                }

                if (lam.isMandatory())
                    mandatoryPropertySet.put(property);
                else
                    generalPropertySet.put(property);
            }
        }

        mandatoryPropertySet.setDisplayName("Mandatory Attributes");
        mandatoryPropertySet.setName("mandatory");  //NOI18N
        generalPropertySet.setDisplayName("General Attributes");
        generalPropertySet.setName("general"); //NOI18N

        if (mandatoryPropertySet.getProperties().length != 0)
            sheet.put(mandatoryPropertySet);

        sheet.put(generalPropertySet);
        return sheet;
    }
}
