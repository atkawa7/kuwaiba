/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.web.components;

import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.Constants;
import com.neotropic.api.forms.ElementListSelectFilter;
import com.neotropic.api.forms.EventDescriptor;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentListSelectFilter extends GraphicalComponent {

    public ComponentListSelectFilter() {
        super(new ListSelectFilter());
    }
    
    @Override
    public ListSelectFilter getComponent() {
        return (ListSelectFilter) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementListSelectFilter) {
            
            ElementListSelectFilter listSelectFilter = new ElementListSelectFilter();
            
            if (listSelectFilter.getItems() != null)
                getComponent().setItems(listSelectFilter.getItems());
                        
            getComponent().setValueChangeListener(new ValueChangeListener() {
                
                @Override
                public void valueChange(HasValue.ValueChangeEvent event) {
                    if (event.getValue() != null && event.getValue() instanceof Set && ((Set) event.getValue()).size() > 0) {
                        
                        if (event.isUserOriginated()) {
                            getComponent().setValue(((Set) event.getValue()).toArray()[0]);
                            
                            fireComponentEvent(new EventDescriptor(
                                Constants.EventAttribute.ONPROPERTYCHANGE, 
                                Constants.Property.VALUE, 
                                getComponent().getValue(), event.getOldValue()));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.ITEMS.equals(event.getPropertyName())) {
                List lst = new ArrayList();
                if (event.getNewValue() != null) {
                    for (Object obj : (List) event.getNewValue())
                        lst.add(obj);
                }
                getComponent().setItems(lst);
            } else if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                getComponent().setValue(event.getNewValue());
            }
        }
    }
            
}
