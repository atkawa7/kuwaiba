/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.nodes;

import org.inventory.core.templates.nodes.properties.DateTypeProperty;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.templates.nodes.actions.TemplateActionsFactory;
import org.inventory.core.templates.nodes.properties.ListTypeProperty;
import org.inventory.core.templates.nodes.properties.PrimitiveTypeProperty;
import org.inventory.core.templates.nodes.properties.TemplateElementPropertyListener;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a template element.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TemplateElementNode extends AbstractNode {

    private final Image defaultIcon = Utils.createRectangleIcon(Utils.DEFAULT_ICON_COLOR, 
            Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);
    
    public TemplateElementNode(LocalObjectLight object) {
        super(new TemplateElementChildren(), Lookups.singleton(object));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {TemplateActionsFactory.getCreateTemplateElementAction(), 
                             null,
                             TemplateActionsFactory.getDeleteTemplateElementAction()};
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return defaultIcon;
    }

    @Override
    public Image getIcon(int type) {
        return defaultIcon;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        LocalObjectLight currentObject = getLookup().lookup(LocalObjectLight.class);
        CommunicationsStub com = CommunicationsStub.getInstance();
        LocalClassMetadata classmetadata = com.getMetaForClass(currentObject.getClassName(), false);
        if (classmetadata == null) 
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            LocalObject templateElement = com.getTemplateElement(currentObject.getClassName(), currentObject.getOid());            
            if (templateElement == null) 
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                templateElement.addPropertyChangeListener(TemplateElementPropertyListener.getInstance());
                Sheet.Set generalSet = Sheet.createPropertiesSet();

                for (LocalAttributeMetadata attributeMetadata : classmetadata.getAttributes()) {
                    PropertySupport property = null;
                    if (!attributeMetadata.isUnique()) { //Unique attributes are not shown
                                                        //as they're not intended to be copied    
                        switch (attributeMetadata.getMapping()) {
                            case Constants.MAPPING_DATE:
                            case Constants.MAPPING_TIMESTAMP:
                                property = new DateTypeProperty(attributeMetadata.getName(),
                                        attributeMetadata.getDisplayName(), attributeMetadata.getDescription(), templateElement);
                                break;
                            case Constants.MAPPING_PRIMITIVE:
                                property = new PrimitiveTypeProperty(attributeMetadata.getName(), attributeMetadata.getType(),
                                        attributeMetadata.getDisplayName(), attributeMetadata.getDescription(), templateElement);
                                break;
                            case Constants.MAPPING_MANYTOONE: //List type
                                List<LocalObjectListItem> list = com.getList(attributeMetadata.getListAttributeClassName(), true, false);
                                if (list == null)
                                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                                else
                                    property = new ListTypeProperty(attributeMetadata.getName(), attributeMetadata.getDisplayName(), 
                                            attributeMetadata.getDescription(), list, templateElement);
                                break;
                            default:
                                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.WARNING_MESSAGE, "Unique and binary attributes are ignored to avoid redundancies");
                        } //Do note that binary
                        if (property != null)
                            generalSet.put(property);
                    }
                }
                sheet.put(generalSet);
            }
        }
        return sheet;
    }

    @Override
    public boolean canRename() {
        return true;
    }
    
    @Override
    public String getName(){
        return getLookup().lookup(LocalObjectLight.class).getName();
    }
    
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalObjectLight.class).toString();
    }

    @Override
    public void setName(String s) {
        TemplateElementPropertyListener.getInstance().propertyChange(
                new PropertyChangeEvent(getLookup().lookup(LocalObjectLight.class), PROP_NAME, getName(), s));
        fireNameChange(getLookup().lookup(LocalObjectLight.class).getName(), s);
        
        if (getSheet() != null)
            setSheet(createSheet());
    }
    
    public static class TemplateElementChildren extends AbstractChildren {
        @Override
        public void addNotify() {
            LocalObjectLight templateElement = getNode().getLookup().lookup(LocalObjectLight.class);
            List<LocalObjectLight> templateElementChildren = CommunicationsStub.getInstance().
                    getTemplateElementChildren(templateElement.getClassName(), templateElement.getOid());
            
            if (templateElementChildren == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else
                setKeys(templateElementChildren);
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight t) {
            return new Node[] {new TemplateElementNode(t)};
        }
    }
}
