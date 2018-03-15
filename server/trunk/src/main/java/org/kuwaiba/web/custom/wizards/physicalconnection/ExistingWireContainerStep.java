/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.wizards.physicalconnection;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.vaadin.teemu.wizards.WizardStep;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ExistingWireContainerStep implements WizardStep {
    private final NewLinkWizard newLinkWizard;
    private DynamicTree treeEndPointA;      
    
    private VerticalLayout content;
    
    public ExistingWireContainerStep(NewLinkWizard newLinkWizard) {
        this.newLinkWizard = newLinkWizard;
    }

    @Override
    public String getCaption() {
        return "Existing Wire Container";
    }

    @Override
    public Component getContent() {
        if (content == null) {
            content = new VerticalLayout();
            content.setMargin(true);
            content.setSizeFull();  
            
            Component trees = initTrees();
            
            if (trees != null) {
                Label lblMessage = new Label("<p><b>Select the objects you'd like to connect.</b></p>", 
                        ContentMode.HTML);
                content.addComponent(lblMessage);
                content.setExpandRatio(lblMessage, 0.1f);
                
                content.addComponent(trees);
                content.setExpandRatio(trees, 0.8f);
            }
        }
        return content;
    }
    private Component initTrees() {
        RemoteObjectLight rootSource = newLinkWizard.getConnectionPolyline().getSource().getRemoteObjectLight();
        InventoryObjectNode rootNodeA = new InventoryObjectNode(rootSource);
        treeEndPointA = new DynamicTree(rootNodeA, newLinkWizard.getParentComponent());
        treeEndPointA.setDragMode(Tree.TreeDragMode.NONE);
        rootNodeA.setTree(treeEndPointA);
                
        HorizontalSplitPanel pnlChooseEndpoints = new HorizontalSplitPanel();
        pnlChooseEndpoints.setFirstComponent(treeEndPointA);
            
        return pnlChooseEndpoints;
    }

    @Override
    public boolean onAdvance() {
        return true;//treeEndPointA.getValue() != null;
    }

    @Override
    public boolean onBack() {
        return true;
    }
    
}
