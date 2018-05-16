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
package org.kuwaiba.apis.forms.components.impl;

import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.ElementButton;
import org.kuwaiba.apis.forms.elements.ElementComboBox;
import org.kuwaiba.apis.forms.elements.ElementDateField;
import org.kuwaiba.apis.forms.elements.ElementGrid;
import org.kuwaiba.apis.forms.elements.ElementGridLayout;
import org.kuwaiba.apis.forms.elements.ElementHorizontalLayout;
import org.kuwaiba.apis.forms.elements.ElementImage;
import org.kuwaiba.apis.forms.elements.ElementLabel;
import org.kuwaiba.apis.forms.elements.ElementListSelectFilter;
import org.kuwaiba.apis.forms.elements.ElementPanel;
import org.kuwaiba.apis.forms.elements.ElementSubform;
import org.kuwaiba.apis.forms.elements.ElementTextArea;
import org.kuwaiba.apis.forms.elements.ElementTextField;
import org.kuwaiba.apis.forms.elements.ElementTree;
import org.kuwaiba.apis.forms.elements.ElementUpload;
import org.kuwaiba.apis.forms.elements.ElementVerticalLayout;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentFactory {
    private static ComponentFactory instance;
    
    private ComponentFactory() {
    }
    
    public static ComponentFactory getInstance() {
        return instance == null ? instance = new ComponentFactory() : instance;
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
            graphicalComponent = new ComponentTree(new TreeWrapper());            
        } else if (element instanceof ElementListSelectFilter) {
            graphicalComponent = new ComponentListSelectFilter();            
        } else if (element instanceof ElementUpload) {
            graphicalComponent = new ComponentUpload();            
        }
        
        if (graphicalComponent != null && element != null) {
            
            graphicalComponent.initFromElement(element);
            
            if (element.getId() != null)
                graphicalComponent.getComponent().setId(element.getId());
            
            element.setElementEventListener(graphicalComponent);
            graphicalComponent.setComponentEventListener(element);
        }
        return graphicalComponent;
////        return component != null && component.getComponent() != null && 
////            component.getComponent() instanceof Component ? component.getComponent() : null;
    }
        
}
