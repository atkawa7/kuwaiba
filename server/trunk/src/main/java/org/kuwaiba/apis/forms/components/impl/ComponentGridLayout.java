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

import org.kuwaiba.apis.forms.components.ComponentContainer;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementGridLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentGridLayout extends GraphicalComponent implements ComponentContainer {
    private LinkedHashMap<AbstractElement, Component> children;
            
    public ComponentGridLayout() {
        super(new GridLayout());
    }
    
    @Override
    public GridLayout getComponent() {
        return (GridLayout) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementGridLayout) {
            ElementGridLayout gridLayout = (ElementGridLayout) element;
            
            getComponent().setColumns(gridLayout.getColumns());
            getComponent().setRows(gridLayout.getRows());
            getComponent().setSpacing(false);
        }
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
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
                    
                    if (component != null) {                    
                        List<Integer> area = element.getArea();

                        if (area != null) {
                            if (area.size() == 2) {
                                int x1 = area.get(0);                                    
                                int y1 = area.get(1);
                                
                                getComponent().addComponent(component, x1, y1);
                            }
                            if (area.size() == 4) {
                                int x1 = area.get(0);                                    
                                int y1 = area.get(1);
                                int x2 = area.get(2);                                    
                                int y2 = area.get(3);
                                
                                getComponent().addComponent(component, x1, y1, x2, y2);
                            }
                        } else
                            getComponent().addComponent(component);
                    }
                }
            }
        }
    }

}
