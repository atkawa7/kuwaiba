/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.NumberField;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

/**
 * Support for Boolean properties
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class BooleanProperty extends AbstractProperty<Boolean>{

    public BooleanProperty(String name, String displayName, String description, Boolean value) {
        super(name, displayName, description, value);
        setType( Constants.DATA_TYPE_BOOLEAN);
    }

    public BooleanProperty(String name, String displayName, String description, Boolean value, boolean readOnly) {
        super(name, displayName, description, value, readOnly);
        setType( Constants.DATA_TYPE_BOOLEAN);
    }
    
    

    @Override
    public AbstractField getAdvancedEditor() {
        return null;
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return false;
    }

    @Override
    public AbstractField getInplaceEditor() {
        Checkbox checkbox = new Checkbox();
        return checkbox;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? AbstractProperty.NULL_LABEL : getValue().toString();
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }

    @Override
    public Boolean getDefaultValue() {
        return false;
    }
}
