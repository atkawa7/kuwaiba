/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.metadataclassnodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.metadataclassnodes.action.CreateClassMetadataAction;
import org.inventory.navigation.applicationnodes.metadataclassnodes.action.RefreshClassMetadataAction;
import org.inventory.navigation.applicationnodes.metadataclassnodes.properties.ClassMetadataNodeProperty;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Represents a node within the data model manager
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassMetadataNode extends AbstractNode implements PropertyChangeListener{
    
    public static final String GENERIC_ICON_PATH="org/inventory/navigation/applicationnodes/res/metadataclassdefaulticon.png";
    protected LocalClassMetadataLight classMetadata;
    
    protected static OpenLocalExplorerAction explorerAction = new OpenLocalExplorerAction();

    protected CommunicationsStub com;

    protected CreateClassMetadataAction createAction;
    protected RefreshClassMetadataAction refreshAction;
    
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
        explorerAction.putValue(OpenLocalExplorerAction.NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_EXPLORE"));
        createAction = new CreateClassMetadataAction(this);
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
                            refreshAction};
    }
        
    @Override
    public boolean canRename(){
        return true;
    }
    
    public boolean refresh(){
        LocalClassMetadata classMetadataRefresh;
        if(classMetadata instanceof LocalClassMetadataLight){
            com.getLightMetaForClass(PROP_NAME, true);
        }
        else{
            com.getMetaForClass(PROP_NAME, true);
        }
        return true;
    }
    
    @Override
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
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

        sheet.put(generalPropertySet);
        
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
