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

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import org.kuwaiba.apis.forms.components.ComponentContainer;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementSubform;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.LinkedHashMap;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentSubform extends GraphicalComponent implements ComponentContainer {
    private LinkedHashMap<AbstractElement, Component> children;
    private Window window;
            
    public ComponentSubform() {
        super(new VerticalLayout());
    }
    
    @Override
    public VerticalLayout getComponent() {
        return (VerticalLayout) super.getComponent();
    }
    
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementSubform) {
            element.setElementEventListener(this);
            getComponent().setSpacing(false);
        }
    }
        
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            if (Constants.Function.OPEN.equals(event.getPropertyName())) {
                
                if (getComponentEventListener() instanceof ElementSubform) {
                    
                    ElementSubform elementSubform = (ElementSubform) getComponentEventListener();
                    
                    if (elementSubform.isEnabled()) {
                        
                        if (UI.getCurrent() != null) {
                            if (window == null) {
                                window = new Window();                        
                                window.setStyleName("subformpopup"); //NOI18N
                                window.setCaption("Subform");
                                window.setDraggable(true);

                                VerticalLayout verticalLayout = new VerticalLayout();
                                verticalLayout.setWidth(100, Unit.PERCENTAGE);
                                verticalLayout.setHeightUndefined();
                                verticalLayout.setSpacing(false);

                                verticalLayout.addComponent(getComponent());
                                verticalLayout.setComponentAlignment(getComponent(), Alignment.MIDDLE_CENTER);

                                window.setContent(verticalLayout);

                                window.setModal(true);
                                window.center();
                            }                                       
                            UI.getCurrent().addWindow(window);
                        }
                    }
                }
            } else if (Constants.Function.CLOSE.equals(event.getPropertyName())) {
                if (window != null)
                    window.close();
            } else if (Constants.Function.CLEAN.equals(event.getPropertyName())) {
                int i = 0;
            }
        } else if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.REPAINT.equals(event.getPropertyName()))
                repaint();
        }  
    }
        
    @Override
    public void addChildren(AbstractElement element, Component component) {
        if (children == null)
            children = new LinkedHashMap();
        
        children.put(element, component);
    }

    @Override
    public LinkedHashMap<AbstractElement, Component> getChildren() {
        return children;
    }

    @Override
    public void repaint() {
        if (getComponent() == null)
            return;
        
        if (getChildren() != null) {
            
            getComponent().removeAllComponents();
            
            for (AbstractElement element : getChildren().keySet()) {
                
                if (!element.isHidden()) {
                    
                    Component component = getChildren().get(element);
                    
                    if (component != null)
                        getComponent().addComponent(component);
                }
            }
        }
    }
    
}
