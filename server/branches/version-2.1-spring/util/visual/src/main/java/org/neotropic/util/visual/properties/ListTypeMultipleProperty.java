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
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.util.visual.general.BoldLabel;


/**
 * Support for multiple list type properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class ListTypeMultipleProperty extends AbstractProperty<List<BusinessObjectLight>>{
    /**
     * The whole list of list items available to fill the list boxes
     */
    List<BusinessObjectLight> listTypes;
    
    public ListTypeMultipleProperty(String name, String displayName, String description, List<BusinessObjectLight> value, List<BusinessObjectLight> listTypes) {
        super(name, displayName, description, value);
        this.listTypes = listTypes;
    }
     
    public ListTypeMultipleProperty(String name, String displayName, String description, List<BusinessObjectLight> value, List<BusinessObjectLight> listTypes, String type) {
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
        BoldLabel title = new BoldLabel("Select Items");
        MultiSelectListBox<BusinessObjectLight> lstBoxEditor = new MultiSelectListBox<>();
        lstBoxEditor.setItems(listTypes);
        lstBoxEditor.select(getValue());
        VerticalLayout content = new VerticalLayout(title, lstBoxEditor);
        return lstBoxEditor;
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return true;
    }

    @Override
    public AbstractField getInplaceEditor() {
        return null;
    }

    @Override
    public String getAsString() {
        if(getValue() == null  || getValue().isEmpty())
            return "Not Items Selected";
        List<BusinessObjectLight> tempList = new ArrayList<>(getValue());
        return (tempList.size() +  " Items Selected");
    }

    @Override
    public String getAsStringToPersist() {
        if (getValue() == null) 
            return ""; 
        
        List<BusinessObjectLight> tempList = new ArrayList<>(getValue());
        
        String idItems = "";
        for (int i = 0; i < tempList.size(); i++) {
            if (i > 0) 
                idItems += ";";
            
            idItems += tempList.get(i).getId();
        }
        return idItems;
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return false;
    }
}
