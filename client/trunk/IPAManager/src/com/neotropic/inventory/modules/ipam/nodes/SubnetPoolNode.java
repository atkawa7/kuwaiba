/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.inventory.modules.ipam.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetPoolAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.DeleteSubnetPoolAction;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * Represent a pool of subnets.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetPoolNode extends AbstractNode implements PropertyChangeListener{
    
    private static final String ICON_PATH="com/neotropic/inventory/modules/res/folder-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    protected Sheet sheet;
    private LocalObjectLight subnetPool;
    protected CommunicationsStub com;

    public SubnetPoolNode(LocalObjectLight subnetPool) {
        super(new SubnetPoolChildren(subnetPool), Lookups.singleton(subnetPool));
        this.subnetPool = subnetPool;
        this.subnetPool.addPropertyChangeListener(this);
        com = CommunicationsStub.getInstance();
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{
            new CreateSubnetAction(this), 
            new CreateSubnetPoolAction(this),
            new DeleteSubnetPoolAction(this)
        };
    }
         
    @Override
    public String getName(){
        return subnetPool.getName() +" ["+subnetPool.getClassName()+"]";
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
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
//        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
//        Sheet.Set administrativePropertySet = Sheet.createPropertiesSet(); //Administrative attributes category
//        LocalClassMetadata meta = com.getMetaForClass(subnetPool.getClassName(), false);
//        if (meta == null) {
//            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
//            return sheet;
//        }
//        LocalObject lo = com.getObjectInfo(subnetPool.getClassName(), subnetPool.getOid());
//        
//        if (lo == null) {
//            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
//            return sheet;
//        }
//        
//        subnetPool.setName(lo.getName());
//        
//        for (LocalAttributeMetadata lam : meta.getAttributes()) {
//            if (lam.isVisible()) {
//                PropertySupport.ReadWrite property = null;
//                int mapping = lam.getMapping();
//                switch (mapping) {
//                    case Constants.MAPPING_DATE:
//                    case Constants.MAPPING_TIMESTAMP:
//                    case Constants.MAPPING_PRIMITIVE:
//                    //Those attributes that are not multiple, but reference another object
//                    //like endpointX in physicalConnections should be ignored, at least by now
//                        if (!lam.getType().equals(LocalObjectLight.class)) {
//                            property = new NativeTypeProperty(
//                                    lam.getName(),
//                                    lam.getType(),
//                                    lam.getDisplayName().isEmpty() ? lam.getName() : lam.getDisplayName(),
//                                    lam.getDescription(), this, lo.getAttribute(lam.getName()));
//                        }
//                        break;
//                    case Constants.MAPPING_MANYTOONE:
//                        //If so, this can be a reference to an object list item or a 1:1 to any other RootObject subclass
//                        List<LocalObjectListItem> list = com.getList(lam.getListAttributeClassName(), true, false);
//                        if (list == null) {
//                            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
//                            return sheet;
//                        }
//                        LocalObjectListItem val = null;
//                        if (lo.getAttribute(lam.getName()) == null) {
//                            val = list.get(0); //None
//                        } else {
//                            for (LocalObjectListItem loli : list) {
//                                if (lo.getAttribute(lam.getName()).equals(loli.getOid())) {
//                                    val = loli;
//                                    break;
//                                }
//                            }
//                        }
//                        property = new ListTypeProperty(
//                                lam.getName(),
//                                lam.getDisplayName().equals("") ? lam.getName() : lam.getDisplayName(),
//                                lam.getDescription(),
//                                list,
//                                this,
//                                val);
//                        break;
//                    case Constants.MAPPING_MANYTOMANY:
//                        property = new NativeTypeProperty(lam.getName(), String.class,
//                                lam.getDisplayName(), "", this, lo.getAttribute(lam.getName()));
//                        break;
//                    default:
//                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Mapping not supported");
//                        return sheet;
//                }
//                generalPropertySet.put(property);
//            }
//        }
//        generalPropertySet.setName("1");
//        administrativePropertySet.setName("2");
//        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
//        administrativePropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ADMINISTRATIVE_ATTRIBUTES"));
//        sheet.put(generalPropertySet);
//        sheet.put(administrativePropertySet);
        return sheet;
    }

    public LocalObjectLight getSubnetPool() {
        return subnetPool;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals(Constants.PROPERTY_NAME))
            fireNameChange("", (String)evt.getNewValue());
    }
}
