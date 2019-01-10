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

import org.kuwaiba.apis.forms.components.ComponentEventListener;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.ElementEventListener;
import com.vaadin.ui.Component;

/**
 * A wrapper to Vaadin components
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class GraphicalComponent implements ElementEventListener {
    private ComponentEventListener componentEventListener;
    private Component component;
    
    public GraphicalComponent(Component component) {
        this.component = component;        
    }
    
    public Component getComponent() {
        return component;
    }
    
    public void setComponent(Component component) {
        this.component = component;        
    }
    
    public ComponentEventListener getComponentEventListener() {
        return componentEventListener;
    }

    public void setComponentEventListener(ComponentEventListener componentEventListener) {
        this.componentEventListener = componentEventListener;        
    }
    
    void fireComponentEvent(EventDescriptor eventDescriptor) {
        componentEventListener.onComponentEvent(eventDescriptor);
    }
    
    public abstract void initFromElement(AbstractElement element);
}
