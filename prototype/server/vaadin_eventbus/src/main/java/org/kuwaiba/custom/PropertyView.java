/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.custom;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 * A property view section show a single property
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@SuppressWarnings("serial")
public class PropertyView extends CustomComponent {
    private TextField txtName;
    private final EventBus eventBus;
    private ValueChange valueChange;
    
    private class ValueChange implements ValueChangeListener {
        public ValueChange() {
        }

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            eventBus.post(event);
        }
    }
        
    
    public PropertyView(final EventBus eventBus) {
        this.eventBus = eventBus;
        valueChange = new ValueChange();
        
        FormLayout frmLayout = new FormLayout();
        
        txtName = new TextField("Name");
        txtName.addValueChangeListener(valueChange);
        
        frmLayout.addComponent(txtName);
        
        setCompositionRoot(frmLayout);
    }
    
    @Subscribe
    public void markerSelected(GoogleMapMarker marker) {
        txtName.removeValueChangeListener(valueChange);
        txtName.setValue(marker.getCaption());        
        txtName.addValueChangeListener(valueChange);
    }
    
    @Subscribe
    public void markerSelected(ItemClickEvent event) {        
        String name = (String) event.getItemId();
        txtName.setValue(name);
    }
    
}
