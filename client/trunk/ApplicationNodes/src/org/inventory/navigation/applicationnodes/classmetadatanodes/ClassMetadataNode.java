/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.classmetadatanodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.AttributeMetadataNode;
import org.inventory.navigation.applicationnodes.classmetadatanodes.action.CreateAttributeAction;
import org.inventory.navigation.applicationnodes.classmetadatanodes.action.DeleteAttributeAction;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.properties.ClassAttributeMetadataProperty;
import org.inventory.navigation.applicationnodes.classmetadatanodes.action.CreateClassAction;
import org.inventory.navigation.applicationnodes.classmetadatanodes.action.DeleteClassAction;
import org.inventory.navigation.applicationnodes.classmetadatanodes.action.RefreshClassMetadataAction;
import org.inventory.navigation.applicationnodes.classmetadatanodes.properties.ClassMetadataNodeProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Represents a class metadata entity as a node within the data model manager
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassMetadataNode extends AbstractNode implements PropertyChangeListener{
    
    public static final String GENERIC_ICON_PATH="org/inventory/navigation/applicationnodes/res/metadataclassdefaulticon.png";
    protected LocalClassMetadataLight classMetadata;
    

    protected CommunicationsStub com;
    protected CreateClassAction createAction;
    protected DeleteClassAction deleteAction;
    protected RefreshClassMetadataAction refreshAction;
    protected CreateAttributeAction createAttributeAction;
    protected DeleteAttributeAction deleteAttributeAction;
    
    protected Sheet sheet;
    protected Image icon;
    private final Image defaultIcon = ImageUtilities.loadImage(GENERIC_ICON_PATH);
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);


    public ClassMetadataNode(LocalClassMetadataLight lcml) {
        super(new ClassMetadataChildren(), Lookups.singleton(lcml));
        this.classMetadata =  lcml;
        this.classMetadata.addPropertyChangeListener(this);
        com = CommunicationsStub.getInstance();
        icon  = classMetadata.getSmallIcon();
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
        return getEditableText();
    }
    
    @Override
    public Image getIcon(int i){
        if (icon==null){
            return defaultIcon;
        }
        return icon;
    }
        
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
    
    private String getEditableText() {
        return classMetadata.getClassName();
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
    
    public boolean refresh(){
        LocalClassMetadataLight classMetadataRefresh;
        
        classMetadataRefresh = com.getLightMetaForClass(PROP_NAME, true);
        
        if(classMetadataRefresh == null)
            return false;
        else
            classMetadata = classMetadataRefresh;
        
        if (this.sheet != null)
            setSheet(createSheet());
        
        return true;
    }
    
    @Override
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General class attributes
        Sheet.Set attributePropertySet = Sheet.createPropertiesSet(); // Class Attributes
        
        LocalClassMetadata lcm = com.getMetaForClass(classMetadata.getClassName(),false);
        if (lcm == null){
            nu.showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return sheet;
        }
        ClassMetadataNodeProperty nameProp = new ClassMetadataNodeProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NAME"), 
                                                                           String.class, lcm.getClassName(), 
                                                                           java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NAME")
                                                                           , "", this);
        ClassMetadataNodeProperty displayNameProp = new ClassMetadataNodeProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DISPLAYNAME"), 
                                                                           String.class, lcm.getDisplayName(), 
                                                                           java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DISPLAYNAME")
                                                                           , "", this);
        ClassMetadataNodeProperty descProp = new ClassMetadataNodeProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DESCRIPTION"), 
                                                                           String.class, lcm.getDescription(), 
                                                                           java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DESCRIPTION")
                                                                           , "", this);
        ClassMetadataNodeProperty abstractProp = new ClassMetadataNodeProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ABSTRACT"), 
                                                                           Boolean.class, lcm.isAbstract(), 
                                                                           java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ABSTRACT")
                                                                           , "", this);
        ClassMetadataNodeProperty inDesignProp = new ClassMetadataNodeProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_INDESIGN"), 
                                                                           Boolean.class, lcm.isInDesign(), 
                                                                           java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_INDESIGN")
                                                                           , "", this);
        ClassMetadataNodeProperty countableProp = new ClassMetadataNodeProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_COUNTABLE"), 
                                                                           Boolean.class, lcm.isCountable(), 
                                                                           java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_COUNTABLE")
                                                                           , "", this);
            
        generalPropertySet.setName("1");
        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
        generalPropertySet.put(nameProp);
        generalPropertySet.put(displayNameProp);
        generalPropertySet.put(descProp);
        generalPropertySet.put(abstractProp);
        generalPropertySet.put(inDesignProp);
        generalPropertySet.put(countableProp);
        
        
        attributePropertySet.setName("2");
        attributePropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CLASS_ATTRIBUTES"));
        
        LocalAttributeMetadata[] attributes = lcm.getAttributes();
        
        if(attributes != null){
            for (LocalAttributeMetadata localAttributeMetadata : attributes){
                ClassAttributeMetadataProperty attrPrprt =  new ClassAttributeMetadataProperty(localAttributeMetadata.getName(), String.class, "Editable attribute", localAttributeMetadata.getName(), localAttributeMetadata.getName(), new AttributeMetadataNode(localAttributeMetadata, classMetadata.getOid()), classMetadata.getOid());
                attributePropertySet.put(attrPrprt);
            }
        }

        sheet.put(generalPropertySet);
        sheet.put(attributePropertySet);
        
        return sheet;
    }
              
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(classMetadata)){
            classMetadata = (LocalClassMetadata)evt.getSource();
            if (evt.getPropertyName().equals(PROP_NAME)){
                setName(evt.getNewValue().toString());
            }
        }
    }
    
    
}
