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
import com.vaadin.flow.component.combobox.ComboBox;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;


/**
 * Support for list type properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class ListTypeProperty extends AbstractProperty<BusinessObjectLight>{
    /**
     * The whole list of list items available to fill the input combo boxes
     */
    List<BusinessObjectLight> listTypes;
    
    public ListTypeProperty(String name, String displayName, String description, BusinessObjectLight value, List<BusinessObjectLight> listTypes) {
        super(name, displayName, description, value);
        this.listTypes = listTypes;
    }

    public ListTypeProperty(String name, String displayName, String description, BusinessObjectLight value, List<BusinessObjectLight> listTypes, String type) {
        super(name, displayName, description, value, type);
        this.listTypes = listTypes;
    }
    
    

    public List<BusinessObjectLight> getListTypes() {
        return listTypes;
    }

    public void setListTypes(List<BusinessObjectLight> listTypes) {
        this.listTypes = listTypes;
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
        ComboBox<BusinessObjectLight> cbxListTypes = new ComboBox<>();
        cbxListTypes.setAllowCustomValue(false);
        cbxListTypes.setItems(listTypes);
        cbxListTypes.setWidthFull();
        return cbxListTypes;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? "Not Set" : getValue().toString();
    }

    @Override
    public String getAsStringToPersist() {
        return getValue() == null ? "" : getValue().getId() + "" ;
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }
}

