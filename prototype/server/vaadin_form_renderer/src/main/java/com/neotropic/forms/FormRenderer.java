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
package com.neotropic.forms;

import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.AbstractElementContainer;
import com.neotropic.api.forms.FormLoader;
import com.neotropic.api.forms.ElementForm;
import com.neotropic.api.forms.ElementSubform;
import com.neotropic.web.components.ComponentContainer;
import com.neotropic.web.components.ComponentFactory;
import com.neotropic.web.components.GraphicalComponent;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormRenderer extends CustomComponent {
    private final VerticalLayout content;
    private final FormLoader builder;
    private HashMap<Component, GraphicalComponent> components = new HashMap();
    
    public FormRenderer(FormLoader builder) {
                        
        this.builder = builder;
        
        if (builder.getRoot() != null && 
            builder.getRoot().getFormStructure() != null &&
            builder.getRoot().getFormStructure().getElementI18N() != null) {
            
            builder.getRoot().getFormStructure().getElementI18N().setLang("en_US");
        }        
        content = new VerticalLayout();
        setCompositionRoot(content);
    }
    
    public void render() {
        content.removeAllComponents();
        
        renderRecursive(builder.getRoot(), content);
        
        builder.fireOnload();
    }
        
    private void renderRecursive(AbstractElement parentElement, Component parentComponent) {
        
        if (parentElement instanceof AbstractElementContainer && 
            ((AbstractElementContainer) parentElement).getChildren() != null) {
            
            for (AbstractElement childElement : ((AbstractElementContainer) parentElement).getChildren()) {

                Component childComponent = null;

                if (childElement instanceof ElementForm) {
                } else {
                    GraphicalComponent childGraphicalComponent = ComponentFactory.getInstance().getComponent(childElement);
                    
                    if (childGraphicalComponent == null)
                        continue;
                    
                    childComponent = childGraphicalComponent.getComponent();
                    
                    if (childComponent == null)
                        continue;
                    
                    components.put(childComponent, childGraphicalComponent);
                                        
                    if (components.get(parentComponent) instanceof ComponentContainer)
                        ((ComponentContainer) components.get(parentComponent)).addChildren(childElement, childComponent);
                }
                
                if (childComponent != null) {
                    
                    if (!(childElement instanceof ElementSubform)) {
                        
                        if (!childElement.isHidden()) {
                            
                            if (parentComponent instanceof Panel) {
                                ((Panel) parentComponent).setContent(childComponent);

                            } else if (parentComponent instanceof AbstractLayout) {
                                if (parentComponent instanceof GridLayout) {

                                    List<Integer> area = childElement.getArea();

                                    if (area != null) {
                                        if (area.size() == 2) {
                                            int x1 = area.get(0);                                    
                                            int y1 = area.get(1);

                                            ((GridLayout) parentComponent).addComponent(childComponent, x1, y1);
                                        }
                                        if (area.size() == 4) {
                                            int x1 = area.get(0);                                    
                                            int y1 = area.get(1);
                                            int x2 = area.get(2);                                    
                                            int y2 = area.get(3);

                                            ((GridLayout) parentComponent).addComponent(childComponent, x1, y1, x2, y2);
                                        }
                                    } else
                                        ((GridLayout) parentComponent).addComponent(childComponent);
                                } else
                                    ((AbstractLayout) parentComponent).addComponent(childComponent);
                            }
                        }
                    }
                    renderRecursive(childElement, childComponent);
                }
            }
        }
    }
    
}
