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
package org.kuwaiba.apis.forms;

import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.AbstractElementContainer;
import org.kuwaiba.apis.forms.elements.FormDefinitionLoader;
import org.kuwaiba.apis.forms.elements.ElementForm;
import org.kuwaiba.apis.forms.elements.ElementSubform;
import org.kuwaiba.apis.forms.components.ComponentContainer;
import org.kuwaiba.apis.forms.components.impl.ComponentFactory;
import org.kuwaiba.apis.forms.components.impl.GraphicalComponent;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.forms.components.impl.ObjectHierarchyProvider;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementMiniApplication;
import org.kuwaiba.apis.forms.elements.FormStructure;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormRenderer extends CustomComponent {
    private final VerticalLayout content;
    private final FormDefinitionLoader formLoader;
    private final HashMap<Component, GraphicalComponent> components = new HashMap();
    private final RemoteProcessInstance processInstance;
    
    public FormRenderer(FormDefinitionLoader formLoader, RemoteProcessInstance processInstance) {
                        
        this.formLoader = formLoader;
        this.processInstance = processInstance;
        
        if (formLoader.getRoot() != null && 
            formLoader.getRoot().getFormStructure() != null &&
            formLoader.getRoot().getFormStructure().getElementI18N() != null) {
            
            formLoader.getRoot().getFormStructure().getElementI18N().setLang("en_US");
        }        
        content = new VerticalLayout();
        setCompositionRoot(content);
    }
    
    public FormStructure getFormStructure() {
        
        if (formLoader.getRoot() != null)
            return formLoader.getRoot().getFormStructure();
        
        return null;
    }
    
    public void render(WebserviceBean wsBean, RemoteSession session) {
        ObjectHierarchyProvider.getInstance().setWebserviceBeanLocal(wsBean);
        ObjectHierarchyProvider.getInstance().setRemoteSession(session);
        
        content.removeAllComponents();
        
        ComponentFactory.getInstance().setWebserviceBean(wsBean);
        
        renderRecursive(formLoader.getRoot(), content);
        
        formLoader.fireOnload(new ScriptQueryExecutorImpl(wsBean, session, processInstance));
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
                    
                    if (!(childElement instanceof ElementSubform) && 
                        !(childElement instanceof ElementMiniApplication && Constants.Attribute.Mode.DETACHED.equals(((ElementMiniApplication) childElement).getMode()))) {
                        
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
                                            ((GridLayout) parentComponent).setComponentAlignment(childComponent, Alignment.TOP_LEFT);
                                        }
                                    } else
                                        ((GridLayout) parentComponent).addComponent(childComponent);
                                } else
                                    ((AbstractLayout) parentComponent).addComponent(childComponent);
                                
                                if (parentComponent instanceof Layout.AlignmentHandler) {
                                    if (childElement.getAlignment() != null) {

                                        ((Layout.AlignmentHandler) parentComponent).setComponentAlignment(childComponent, getAlignment(childElement.getAlignment()));
                                    }
                                }
                            }
                        }
                    }
                    renderRecursive(childElement, childComponent);
                }
            }
        }
    }
    
    private Alignment getAlignment(String key) {
        
        switch (key) {
            case Constants.Property.Alignment.BOTTOM_CENTER:
                return Alignment.BOTTOM_CENTER;
            case Constants.Property.Alignment.BOTTOM_LEFT:
                return Alignment.BOTTOM_LEFT;
            case Constants.Property.Alignment.BOTTOM_RIGHT:
                return Alignment.BOTTOM_RIGHT;
            case Constants.Property.Alignment.MIDDLE_CENTER:
                return Alignment.MIDDLE_CENTER;
            case Constants.Property.Alignment.MIDDLE_LEFT:
                return Alignment.MIDDLE_LEFT;
            case Constants.Property.Alignment.MIDDLE_RIGHT:
                return Alignment.MIDDLE_RIGHT;
            case Constants.Property.Alignment.TOP_CENTER:
                return Alignment.TOP_CENTER;
            case Constants.Property.Alignment.TOP_LEFT:
                return Alignment.TOP_LEFT;
            case Constants.Property.Alignment.TOP_RIGHT:
                return Alignment.TOP_RIGHT;
            default:
                return Alignment.MIDDLE_LEFT;
        }
    }
    
}
