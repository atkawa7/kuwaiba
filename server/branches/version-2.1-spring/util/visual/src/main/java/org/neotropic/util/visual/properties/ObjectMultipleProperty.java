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
import org.neotropic.kuwaiba.core.i18n.TranslationService;
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
    
    public static String NOT_ITEMS_SELECTED_LABEL = "Not Items Selected";
    public static String ITEMS_SELECTED_LABEL = "Items Selected";
    public static String SELECT_ITEMS_LABEL = "Select Items";
    
    public ObjectMultipleProperty(String name, String displayName, String description, List value, List items, String type, TranslationService ts) {
        super(name, displayName, description, value, ts);
        this.allItems = items;
        setType(type);
        NOT_ITEMS_SELECTED_LABEL = ts.getTranslatedString("module.propertysheet.labels.not-items-selected");
        ITEMS_SELECTED_LABEL = ts.getTranslatedString("module.propertysheet.labels.items-selected");
        SELECT_ITEMS_LABEL = ts.getTranslatedString("module.propertysheet.labels.select-items");
    }

    public List getListTypes() {
        return allItems;
    }

    public void setListTypes(List listTypes) {
        this.allItems = listTypes;
    }

    @Override
    public AbstractField getAdvancedEditor() {     
        BoldLabel title = new BoldLabel(SELECT_ITEMS_LABEL);
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
            return NOT_ITEMS_SELECTED_LABEL;
        List<Object> tempList = new ArrayList<>(getValue());
        return (tempList.size() + " " + ITEMS_SELECTED_LABEL);
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

