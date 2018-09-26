/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.core.variables.nodes;

import javax.swing.Action;
import org.inventory.communications.core.LocalConfigurationVariable;
import org.kuwaiba.core.variables.nodes.actions.ConfigurationVariablesActionFactory;
import org.kuwaiba.core.variables.nodes.properties.ConfigurationVariableNativeTypeProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a configuration variable
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConfigurationVariableNode extends AbstractNode {

    public ConfigurationVariableNode(LocalConfigurationVariable configVariable) {
        super(Children.LEAF, Lookups.singleton(configVariable));
    }
    
    @Override
    public String getName() {
        return getLookup().lookup(LocalConfigurationVariable.class).getName();
    }
    
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalConfigurationVariable.class).getName();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ConfigurationVariablesActionFactory.getDeleteConfigurationVariableAction() };
    }
    
    @Override
    public Sheet createSheet() {
        Sheet aSheet = Sheet.createDefault();
        Sheet.Set aSet = new Sheet.Set();
        aSet.setName("General Properties");
        
        LocalConfigurationVariable configVariable = getLookup().lookup(LocalConfigurationVariable.class);
        try {
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, String.class, "name"));
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, String.class, "description"));
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, Boolean.class, "masked"));
            aSet.put(new ConfigurationVariableNativeTypeProperty(configVariable, String.class, "value"));
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
}
