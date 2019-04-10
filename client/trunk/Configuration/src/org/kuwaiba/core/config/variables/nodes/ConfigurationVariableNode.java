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

package org.kuwaiba.core.config.variables.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalConfigurationVariable;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.core.config.variables.nodes.actions.ConfigurationVariablesActionFactory;
import org.kuwaiba.core.config.variables.properties.ConfigurationVariableNativeTypeProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a configuration variable
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ConfigurationVariableNode extends AbstractNode implements VetoableChangeListener {

    public ConfigurationVariableNode(LocalConfigurationVariable configVariable) {
        super(Children.LEAF, Lookups.singleton(configVariable));
        setDisplayName(configVariable.getName());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ConfigurationVariablesActionFactory.getDeleteConfigurationVariableAction() };
    }

    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalConfigurationVariable.class).getName();
    }

    @Override
    public String getName() {
        return getLookup().lookup(LocalConfigurationVariable.class).getName();
    }
    
    
    
    @Override
    public Sheet createSheet() {
        Sheet aSheet = Sheet.createDefault();
        Sheet.Set aSet = new Sheet.Set();
        aSet.setName("General Properties");
        
        LocalConfigurationVariable configVariable = getLookup().lookup(LocalConfigurationVariable.class);
        try {
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, String.class, "name", this));
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, String.class, "description", this));
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, boolean.class, "masked", this));
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, String.class, "value", this));
        } catch (NoSuchMethodException ex) {} //Should not happen

        aSheet.put(aSet);
        
        return aSheet;
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
            vetoableChange(new PropertyChangeEvent(getLookup().lookup(LocalConfigurationVariable.class), Constants.PROPERTY_NAME, getName(), name));
            getLookup().lookup(LocalConfigurationVariable.class).setName(name);
            
            if (getSheet() != null)
                setSheet(createSheet());
        } catch (PropertyVetoException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (!CommunicationsStub.getInstance().updateConfigurationVariable(((LocalConfigurationVariable)evt.getSource()).getName(), 
                evt.getPropertyName(), String.valueOf(evt.getNewValue())))
            throw new PropertyVetoException(CommunicationsStub.getInstance().getError(), evt);
        else {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {
                getLookup().lookup(LocalConfigurationVariable.class).setName((String)evt.getNewValue());
                fireNameChange("", (String)evt.getNewValue());
            }
        }
    }
}
