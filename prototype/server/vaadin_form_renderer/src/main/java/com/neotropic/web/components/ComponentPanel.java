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
import com.neotropic.api.forms.ElementPanel;
import com.neotropic.api.forms.EventDescriptor;
import com.vaadin.ui.Panel;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentPanel extends GraphicalComponent {

    public ComponentPanel() {
        super(new Panel());
    }
        
    @Override
    public Panel getComponent() {
        return (Panel) super.getComponent();
    }
    
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementPanel) {
            ElementPanel panel = (ElementPanel) element;
            
            if (panel.getHeight() != null)
                getComponent().setHeight(panel.getHeight());
            
            if (panel.getWidth() != null)
                getComponent().setWidth(panel.getWidth());
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        //TODO: implements events
    }
    
}
