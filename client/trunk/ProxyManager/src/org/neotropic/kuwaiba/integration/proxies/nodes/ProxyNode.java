/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.integration.proxies.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Date;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalInventoryProxy;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.nodes.DateTypeProperty;
import org.inventory.core.services.api.nodes.MultipleListTypeProperty;
import org.inventory.core.services.api.nodes.NativeTypeProperty;
import org.inventory.core.services.api.nodes.SingleListTypeProperty;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.neotropic.kuwaiba.integration.proxies.nodes.actions.ProxiesActionFactory;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * A node representing an inventory proxy.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ProxyNode extends ObjectNode implements VetoableChangeListener {
    private static final Image ICON = ImageUtilities.loadImage("org/neotropic/kuwaiba/integration/proxies/res/proxyNodeIcon.png");
    
    public ProxyNode(LocalInventoryProxy inventoryProxy) {
        super(Children.LEAF, Lookups.singleton(inventoryProxy));
        setDisplayName(inventoryProxy.getName());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { 
            ProxiesActionFactory.getRelateProxyToProjectAction(),
            ProxiesActionFactory.getReleaseProxyFromProject(),
            null,
            ProxiesActionFactory.getDeleteProxyAction(),
        };
    }

    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalInventoryProxy.class).getName();
    }

    @Override
    public String getName() {
        return getLookup().lookup(LocalInventoryProxy.class).getName();
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        Sheet.Set mandatoryPropertySet = Sheet.createPropertiesSet(); //Set with the mandatory attributes
        
        LocalInventoryProxy proxy = getLookup().lookup(LocalInventoryProxy.class);
        
        if (proxy == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            return sheet;
        }
        
        LocalClassMetadata metadata = com.getMetaForClass(proxy.getClassName(), false);
        
        for (LocalAttributeMetadata lam : metadata.getAttributes()) {
            if (lam.isVisible()) {
                PropertySupport.ReadWrite property = null;
                int mapping = lam.getMapping();
                switch (mapping) {
                    case Constants.MAPPING_TIMESTAMP:
                    case Constants.MAPPING_DATE:
                        property = new DateTypeProperty((Date)proxy.getAttribute(lam.getName()) , 
                                lam.getName(), Date.class, lam.getDisplayName(),
                                lam.getDescription(), this);
                        break;
                    case Constants.MAPPING_PRIMITIVE:
                        if (!lam.getType().equals(LocalObjectLight.class)) {
                            property = new NativeTypeProperty(
                                    lam.getName(),
                                    lam.getType(),
                                    lam.getDisplayName().isEmpty() ? lam.getName() : lam.getDisplayName(),
                                    lam.getDescription(), proxy.getAttribute(lam.getName()), this);
                        }
                        break;
                    case Constants.MAPPING_MANYTOONE: {
                        //If so, this can be a reference to an object list item or a 1:1 to any other RootObject subclass
                        List<LocalObjectListItem> list = com.getList(lam.getListAttributeClassName(), true, false);
                        if (list == null) {
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                            return sheet;
                        }
                        LocalObjectListItem val = null;
                        if (proxy.getAttribute(lam.getName()) == null) 
                            val = list.get(0); //None
                        else {
                            for (LocalObjectListItem loli : list) {
                                if (proxy.getAttribute(lam.getName()).equals(loli)) {
                                    val = loli;
                                    break;
                                }
                            }
                        }
                        property = new SingleListTypeProperty(
                                lam.getName(),
                                lam.getDisplayName(),
                                lam.getDescription(),
                                list,
                                this,
                                val);
                        break;
                    }
                    case Constants.MAPPING_MANYTOMANY: {
                        List<LocalObjectListItem> list = com.getList(lam.getListAttributeClassName(), false, false);
                        if (list == null) {
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                            return sheet;
                        }
                        
                        property = new MultipleListTypeProperty(
                                lam.getName(),
                                lam.getDisplayName(),
                                lam.getDescription(),
                                list,
                                this,
                                (List<LocalObjectListItem>)proxy.getAttribute(lam.getName()));
                        break;
                    }
                    default:
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "Mapping not supported");
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
    
    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public boolean canCut() {
        return false;
    }
    
    @Override
    public boolean canRename() {
        return true;
    }
    
    @Override
    public void setName(String name) {
        try {
            vetoableChange(new PropertyChangeEvent(getLookup().lookup(LocalInventoryProxy.class), Constants.PROPERTY_NAME, getName(), name));
            getLookup().lookup(LocalInventoryProxy.class).setName(name);
            if (getSheet() != null)
                setSheet(createSheet());
        } catch (PropertyVetoException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        LocalInventoryProxy selectedProxy = getLookup().lookup(LocalInventoryProxy.class);
        if (!CommunicationsStub.getInstance().updateProxy(selectedProxy.getClassName(),
                selectedProxy.getId(), evt.getPropertyName(), (String)evt.getNewValue()))
            throw new PropertyVetoException(CommunicationsStub.getInstance().getError(), evt);
        else {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {
                getLookup().lookup(LocalInventoryProxy.class).setName((String)evt.getNewValue());
                fireNameChange("", (String)evt.getNewValue());
            }
        }
    }
}
