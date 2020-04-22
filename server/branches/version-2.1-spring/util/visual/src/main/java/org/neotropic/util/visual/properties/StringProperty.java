/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;


/**
 * Support for String like properties
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class StringProperty extends AbstractProperty<String>{

    public StringProperty(String name, String displayName, String description, String value) {
        super(name, displayName, description, value);
    }

    public StringProperty(String name, String displayName, String description, String value, String type) {
        super(name, displayName, description, value, type);
    }
       

    @Override
    public AbstractField getAdvancedEditor() {
        TextArea txtArea = new TextArea(this.getName(), this.getValue(), "...");  
        txtArea.setWidthFull();
        txtArea.setMinHeight("300px");
        return txtArea;
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return true;
    }

    @Override
    public AbstractField getInplaceEditor() {
        TextField txtPropertyEditor = new TextField();
        txtPropertyEditor.setSizeFull();
        return txtPropertyEditor;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? "Not Set" : getValue();
    }

    @Override
    public String getAsStringToPersist() {
        return getValue(); 
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }
}
