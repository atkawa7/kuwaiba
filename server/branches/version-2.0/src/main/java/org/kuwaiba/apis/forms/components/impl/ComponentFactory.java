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

import com.vaadin.ui.UI;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.ElementButton;
import org.kuwaiba.apis.forms.elements.ElementCheckBox;
import org.kuwaiba.apis.forms.elements.ElementComboBox;
import org.kuwaiba.apis.forms.elements.ElementDateField;
import org.kuwaiba.apis.forms.elements.ElementGrid;
import org.kuwaiba.apis.forms.elements.ElementGridLayout;
import org.kuwaiba.apis.forms.elements.ElementHorizontalLayout;
import org.kuwaiba.apis.forms.elements.ElementImage;
import org.kuwaiba.apis.forms.elements.ElementLabel;
import org.kuwaiba.apis.forms.elements.ElementListSelectFilter;
import org.kuwaiba.apis.forms.elements.ElementMiniApplication;
import org.kuwaiba.apis.forms.elements.ElementPanel;
import org.kuwaiba.apis.forms.elements.ElementSubform;
import org.kuwaiba.apis.forms.elements.ElementTextArea;
import org.kuwaiba.apis.forms.elements.ElementTextField;
import org.kuwaiba.apis.forms.elements.ElementTree;
import org.kuwaiba.apis.forms.elements.ElementUpload;
import org.kuwaiba.apis.forms.elements.ElementVerticalLayout;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentFactory {
    private static ComponentFactory instance;
    
    private ComponentFactory() {
    }
    
    public static ComponentFactory getInstance() {
        return instance == null ? instance = new ComponentFactory() : instance;
    }
    
    private WebserviceBean wsBean;
    
    public void setWebserviceBean(WebserviceBean wsBean) {
        this.wsBean = wsBean;
    }
    
    public GraphicalComponent getComponent(AbstractElement element) {
        GraphicalComponent graphicalComponent = null;
        
        if (element instanceof ElementGridLayout) {
            graphicalComponent = new ComponentGridLayout();
        } else if (element instanceof ElementVerticalLayout) {
            graphicalComponent = new ComponentVerticalLayout();
        } else if (element instanceof ElementLabel) {
            graphicalComponent = new ComponentLabel();
        } else if (element instanceof ElementTextField) {
            graphicalComponent = new ComponentTextField();            
        } else if (element instanceof ElementTextArea) {
            graphicalComponent = new ComponentTextArea();            
        } else if (element instanceof ElementDateField) {
            graphicalComponent = new ComponentDateField();            
        } else if (element instanceof ElementComboBox) {
            graphicalComponent = new ComponentComboBox();            
        } else if (element instanceof ElementGrid) {
            graphicalComponent = new ComponentGrid();            
        } else if (element instanceof ElementButton) {
            graphicalComponent = new ComponentButton();            
        } else if (element instanceof ElementHorizontalLayout) {
            graphicalComponent = new ComponentHorizontalLayout();            
        } else if (element instanceof ElementImage) {
            graphicalComponent = new ComponentImage();
        } else if (element instanceof ElementSubform) {
            graphicalComponent = new ComponentSubform();            
        } else if (element instanceof ElementPanel) {
            graphicalComponent = new ComponentPanel();            
        } else if (element instanceof ElementTree) {
            
            BasicTree dynamicTree = new BasicTree(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                        @Override
                        public List<RemoteObjectLight> getChildren(RemoteObjectLight parentObject) {
                            try {
                                RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
                                return wsBean.getObjectChildren(
                                        parentObject.getClassName(), 
                                        parentObject.getId(), -1, session.getIpAddress(), 
                                        session.getSessionId());
                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getLocalizedMessage());
                                return new ArrayList<>();
                            }
                        }
                    }, new BasicIconGenerator(wsBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")),
                    new AbstractNode<RemoteObjectLight>(new RemoteObjectLight(Constants.DUMMY_ROOT, "-1", "Navigation Root")) {
                        @Override
                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                        @Override
                        public void refresh(boolean recursive) { }
                }
                );
            
            graphicalComponent = new ComponentTree(dynamicTree);
            
        } else if (element instanceof ElementListSelectFilter) {
            graphicalComponent = new ComponentListSelectFilter();            
        } else if (element instanceof ElementUpload) {
            graphicalComponent = new ComponentUpload();            
        } else if (element instanceof ElementMiniApplication) {
            graphicalComponent = new ComponentMiniApplication(wsBean);            
        } else if (element instanceof ElementCheckBox) {
            graphicalComponent = new ComponentCheckBox();                                    
        }
        
        if (graphicalComponent != null && element != null) {
            
            graphicalComponent.initFromElement(element);
            
            if (element.getId() != null)
                graphicalComponent.getComponent().setId(element.getId());
            
            if (element.getWidth() != null)
                graphicalComponent.getComponent().setWidth(element.getWidth());
            
            if (element.getHeight() != null)
                graphicalComponent.getComponent().setHeight(element.getHeight());
                        
            graphicalComponent.getComponent().setVisible(!element.isHidden());
            
            element.setElementEventListener(graphicalComponent);
            graphicalComponent.setComponentEventListener(element);
        }
        return graphicalComponent;
    }
        
}
