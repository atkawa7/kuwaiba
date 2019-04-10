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

package org.kuwaiba.core.config.validators.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalValidatorDefinition;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.core.config.validators.nodes.actions.ValidatorDefinitionsActionFactory;
import org.kuwaiba.core.config.validators.nodes.properties.ValidatorDefinitionNativeTypeProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a validator definition
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ValidatorDefinitionNode extends AbstractNode implements VetoableChangeListener {

    public ValidatorDefinitionNode(LocalValidatorDefinition validatorDefinition) {
        super(Children.LEAF, Lookups.singleton(validatorDefinition));
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ValidatorDefinitionsActionFactory.getDeleteValidatorDefinitionAction() };
    }

    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalValidatorDefinition.class).getName();
    }

    @Override
    public String getName() {
        return getLookup().lookup(LocalValidatorDefinition.class).getName();
    }
    
    @Override
    public Sheet createSheet() {
        Sheet aSheet = Sheet.createDefault();
        Sheet.Set aSet = new Sheet.Set();
        aSet.setName("General Properties");
        
        LocalValidatorDefinition validatorDefinition = getLookup().lookup(LocalValidatorDefinition.class);
        try {
            aSet.put(new ValidatorDefinitionNativeTypeProperty(validatorDefinition, String.class, "name", this));
            aSet.put(new ValidatorDefinitionNativeTypeProperty(validatorDefinition, String.class, "description", this));
            aSet.put(new ValidatorDefinitionNativeTypeProperty(validatorDefinition, String.class, "script", this));
            aSet.put(new ValidatorDefinitionNativeTypeProperty(validatorDefinition, boolean.class, "enabled", this));
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
            vetoableChange(new PropertyChangeEvent(getLookup().lookup(LocalValidatorDefinition.class), Constants.PROPERTY_NAME, getName(), name));
            getLookup().lookup(LocalValidatorDefinition.class).setName(name);
            
            if (getSheet() != null)
                setSheet(createSheet());
        } catch (PropertyVetoException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        LocalValidatorDefinition validatorDefinition = (LocalValidatorDefinition)evt.getSource();
        if (!CommunicationsStub.getInstance().updateValidatorDefinition(validatorDefinition.getId(), evt.getPropertyName().equals(Constants.PROPERTY_NAME) ? (String)evt.getNewValue() : null, 
                evt.getPropertyName().equals(Constants.PROPERTY_DESCRIPTION) ? (String)evt.getNewValue() : null, 
                validatorDefinition.getClassToBeApplied(), 
                evt.getPropertyName().equals(Constants.PROPERTY_SCRIPT) ? (String)evt.getNewValue() : null, 
                evt.getPropertyName().equals(Constants.PROPERTY_ENABLED) ? (boolean)evt.getNewValue() : null))
            throw new PropertyVetoException(CommunicationsStub.getInstance().getError(), evt);
        else {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {
                getLookup().lookup(LocalValidatorDefinition.class).setName((String)evt.getNewValue());
                fireNameChange("", (String)evt.getNewValue());
            }
        }
    }
}
