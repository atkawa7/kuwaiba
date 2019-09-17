/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms.components.impl;

import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.ComboBox.CaptionFilter;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A list select with a field to filter the listed items
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ListSelectFilter<T> extends VerticalLayout {
    private int width = 400;
    private List<T> items;
    private final TextField textField;
    private final ListSelect<T> listSelect;
    private ValueChangeListener<Set<T>> valueChangeListener;
    private T value;
    
    private final CaptionFilter captionFilter = new CaptionFilter() {        
        
        @Override
        public boolean test(String itemCaption, String filterText) {
            
            if (itemCaption == null && filterText == null)
                return false;
            
            return itemCaption.toLowerCase().contains(filterText.toLowerCase());
        }
    };
    
    public ListSelectFilter() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();                                
        
        textField = new TextField();
                                        
        listSelect = new ListSelect();
        listSelect.setSizeFull();
                        
        setWidth(width + "px");
                        
        textField.addValueChangeListener(new ValueChangeListener<String>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<String> event) {
                if (items != null) {
                    List<T> filteredItems = new ArrayList();
                    
                    Iterator<T> iterator = items.iterator();
                    while (iterator.hasNext()) {
                        T element = iterator.next();
                        
                        if (captionFilter.test(element != null ? element.toString() : null, event.getValue()))
                            filteredItems.add(element);
                    }
                    listSelect.setItems(filteredItems);
                }
            }
        });
                
        Label label = new Label(VaadinIcons.SEARCH.getHtml());
        label.setWidth("50px");
        label.setContentMode(ContentMode.HTML);
        
        textField.setWidth((width - 100) + "px");
                
        horizontalLayout.addComponent(textField);
        horizontalLayout.addComponent(label);
                
        this.addComponent(horizontalLayout);
        this.addComponent(listSelect);
    }
    
    public void setWidth(int width) {
        this.width = width;
        setWidth(width + "px");
        textField.setWidth((width - 100) + "px");
    }
    
    public ValueChangeListener<Set<T>> getValueChangeListener() {
        return valueChangeListener;
    }
    
    public void setValueChangeListener(ValueChangeListener<Set<T>> valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
        listSelect.addValueChangeListener(valueChangeListener);
    }
    
    public ListSelectFilter(int rows) {
        this();
        listSelect.setRows(rows);
    }
    
    public ListSelectFilter(List<T> items) {
        this();
        this.items = items;
        listSelect.setItems(items);
    }
    
    public ListSelectFilter(int rows, List<T> items) {
        this(items);
        listSelect.setRows(rows);
    }
    
    public List<T> getItems() {
        return items;
    }
    
    public void setItems(List<T> items) {
        this.items = items;
        listSelect.setItems(items);
    }
    
    public int getRows() {
        return listSelect.getRows();        
    }
    
    public void setRows(int rows) {
        listSelect.setRows(rows);
    }
    
    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
}
