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
package org.inventory.customization.classhierarchy.nodes;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.classhierarchy.nodes.properties.ClassAttributeMetadataProperty;
import org.inventory.customization.classhierarchy.nodes.actions.CreateAttributeAction;
import org.inventory.customization.classhierarchy.nodes.actions.CreateClassAction;
import org.inventory.customization.classhierarchy.nodes.actions.DeleteAttributeAction;
import org.inventory.customization.classhierarchy.nodes.actions.DeleteClassAction;
import org.inventory.customization.classhierarchy.nodes.actions.RefreshClassMetadataAction;
import org.inventory.customization.classhierarchy.nodes.properties.ClassMetadataProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Represents a ClassMetadata entity as a node within the data model manager
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class ClassMetadataNode extends AbstractNode implements PropertyChangeListener{
       
    protected LocalClassMetadataLight classMetadata;
    
    protected CommunicationsStub com;
    protected CreateClassAction createAction;
    protected DeleteClassAction deleteAction;
    protected RefreshClassMetadataAction refreshAction;
    protected CreateAttributeAction createAttributeAction;
    protected DeleteAttributeAction deleteAttributeAction;
    protected Sheet sheet;
    private final Image defaultIcon = Utils.createRectangleIcon(Utils.DEFAULT_CLASS_ICON_COLOR, 
            Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);

    public ClassMetadataNode(LocalClassMetadataLight lcml) {
        super(new ClassMetadataChildren(), Lookups.singleton(lcml));
        this.classMetadata =  lcml;
        this.classMetadata.addPropertyChangeListener(this);
        com = CommunicationsStub.getInstance();
        createAction = new CreateClassAction(this);
        deleteAction = new DeleteClassAction(this);
        createAttributeAction = new CreateAttributeAction(this);
        deleteAttributeAction = new DeleteAttributeAction(this);
    }
    
    public LocalClassMetadataLight getClassMetadata() {
        return classMetadata;
    }
       
    @Override
    public String getName(){     
        return classMetadata.getClassName();
    }
    
    @Override
    public Image getIcon(int i){
        return defaultIcon;
    }
        
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
        
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{createAction,
                            deleteAction,
                            refreshAction,
                            createAttributeAction,
                            deleteAttributeAction};
    }
        
    @Override
    public boolean canRename(){
        return true;
    }

    @Override
    public void setName(String newName) {
        if(com.setClassMetadataProperties(classMetadata.getOid(), newName,  null, 
                null, null, null, -1, null, null, null, null )){
            classMetadata.setClassName(newName);
            refresh();
        }else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }
    
    public void refresh(){
        LocalClassMetadataLight classMetadataRefresh;
        
        classMetadataRefresh = com.getMetaForClass(classMetadata.getOid(), true);
        
        if(classMetadataRefresh == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else{
            classMetadata = classMetadataRefresh;
            if (this.sheet != null)
                setSheet(createSheet());
            fireNameChange("", getName());
        }
    }
    
    @Override
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General class attributes
        Sheet.Set attributePropertySet = Sheet.createPropertiesSet(); // Class Attributes
        
        LocalClassMetadata lcm = com.getMetaForClass(classMetadata.getClassName(), true);
        if (lcm == null){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return sheet;
        }
        ClassMetadataProperty nameProp = new ClassMetadataProperty(Constants.PROPERTY_NAME, 
                                                                           String.class, lcm.getClassName(), "Name", "", this);
        ClassMetadataProperty displayNameProp = new ClassMetadataProperty(Constants.PROPERTY_DISPLAYNAME, 
                                                                           String.class, lcm.getDisplayName(), 
                                                                           "Display Name", "", this);
        ClassMetadataProperty descProp = new ClassMetadataProperty(Constants.PROPERTY_DESCRIPTION, 
                                                                           String.class, lcm.getDescription(), 
                                                                           "Description", "", this);
        ClassMetadataProperty abstractProp = new ClassMetadataProperty(Constants.PROPERTY_ABSTRACT, 
                                                                           Boolean.class, lcm.isAbstract(), 
                                                                           "Abstract", "", this);
        ClassMetadataProperty inDesignProp = new ClassMetadataProperty(Constants.PROPERTY_INDESIGN, 
                                                                           Boolean.class, lcm.isInDesign(), 
                                                                           "In Design Stage", "", this);
        ClassMetadataProperty countableProp = new ClassMetadataProperty(Constants.PROPERTY_COUNTABLE, 
                                                                           Boolean.class, lcm.isCountable(), 
                                                                           "Countable", "", this);
        ClassMetadataProperty colorProp = new ClassMetadataProperty(Constants.PROPERTY_COLOR, 
                                                                           Color.class, lcm.getColor(), 
                                                                           "Color", "", this);
        ClassMetadataProperty smallIconProp = new ClassMetadataProperty(Constants.PROPERTY_SMALLICON, 
                                                                             Byte.class, null, 
                                                                           "Small Icon", "", this);
        ClassMetadataProperty iconProp = new ClassMetadataProperty(Constants.PROPERTY_ICON, 
                                                                           Byte.class, null, 
                                                                           "Icon", "", this);
        generalPropertySet.setName("1");
        generalPropertySet.setDisplayName("General Attributes");
        generalPropertySet.put(nameProp);
        generalPropertySet.put(displayNameProp);
        generalPropertySet.put(descProp);
        generalPropertySet.put(abstractProp);
        generalPropertySet.put(inDesignProp);
        generalPropertySet.put(countableProp);
        generalPropertySet.put(colorProp);
        generalPropertySet.put(smallIconProp);
        generalPropertySet.put(iconProp);
        
        attributePropertySet.setName("2");
        attributePropertySet.setDisplayName("Class Attributes");
        attributePropertySet.setExpert(true);
        LocalAttributeMetadata[] attributes = lcm.getAttributes();
        
        if(attributes != null){
            for (LocalAttributeMetadata localAttributeMetadata : attributes){
                ClassAttributeMetadataProperty attrPrprt =  new ClassAttributeMetadataProperty(localAttributeMetadata, this);
                attributePropertySet.put(attrPrprt);
            }
        }

        sheet.put(generalPropertySet);
        sheet.put(attributePropertySet);
        
        return sheet;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.classMetadata != null ? this.classMetadata.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return getClassMetadata().getOid() == ((ClassMetadataNode) obj).getClassMetadata().getOid();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Constants.PROPERTY_NAME))
            fireNameChange("", (String)evt.getNewValue());
    }
}