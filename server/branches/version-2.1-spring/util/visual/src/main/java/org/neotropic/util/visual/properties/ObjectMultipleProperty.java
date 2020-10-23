/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>
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
import java.util.ArrayList;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.util.visual.general.BoldLabel;

/**
 * Support for multiple object properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class ObjectMultipleProperty extends AbstractProperty<List>{
    /**
     * The whole list of list items available to fill the list boxes
     */
    List<Object> allItems;
    
    public ObjectMultipleProperty(String name, String displayName, String description, List value, List items) {
        super(name, displayName, description, value);
        this.allItems = items;
        setType(Constants.DATA_TYPE_LIST_TYPE);
    }

    public List getListTypes() {
        return allItems;
    }

    public void setListTypes(List listTypes) {
        this.allItems = listTypes;
    }

    @Override
    public AbstractField getAdvancedEditor() {     
        BoldLabel title = new BoldLabel(AbstractProperty.SELECT_ITEMS_LABEL);
        MultiSelectListBox lstBoxEditor = new MultiSelectListBox<>();
        lstBoxEditor.setItems(allItems);
        lstBoxEditor.select(getValue());
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
            return AbstractProperty.NOT_ITEMS_SELECTED_LABEL;
        List<Object> tempList = new ArrayList<>(getValue());
        return (tempList.size() + " " + AbstractProperty.ITEMS_SELECTED_LABEL);
    } 
    
    @Override
    public boolean supportsInplaceEditor() {
        return false;
    }

    @Override
    public List getDefaultValue() {
       return new ArrayList<>();
    }
}

