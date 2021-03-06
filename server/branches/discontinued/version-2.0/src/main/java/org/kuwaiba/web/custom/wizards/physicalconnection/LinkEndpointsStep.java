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
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.vaadin.teemu.wizards.WizardStep;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class LinkEndpointsStep implements WizardStep {
    private final NewLinkWizard newLinkWizard;
    private DynamicTree treeEndPointA;
    private DynamicTree treeEndPointB;
    
    private VerticalLayout content;
    
    public LinkEndpointsStep(NewLinkWizard newLinkWizard) {
        this.newLinkWizard = newLinkWizard;
    }
    
    public DynamicTree getTreeEndPointA() {
        return treeEndPointA;
    }
    
    public DynamicTree getTreeEndPointB() {
        return treeEndPointB;
    }

    @Override
    public String getCaption() {
        return "Link endpoints";
    }

    @Override
    public Component getContent() {
        if (content == null) {
            content = new VerticalLayout();
            content.setMargin(true);
            content.setSizeFull();  
            
            Component trees = initTrees();
            
            if (trees != null) {
                Label lblMessage = new Label("<p><b>Select the ports you'd like to connect.</b></p>", 
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
        
        RemoteObjectLight rootTarget = newLinkWizard.getConnectionPolyline().getTarget().getRemoteObjectLight();
        InventoryObjectNode rootNodeB = new InventoryObjectNode(rootTarget);
        treeEndPointB = new DynamicTree(rootNodeB, newLinkWizard.getParentComponent());
        treeEndPointB.setDragMode(Tree.TreeDragMode.NONE);
        rootNodeB.setTree(treeEndPointB);
        
        HorizontalSplitPanel pnlChooseEndpoints = new HorizontalSplitPanel();
        pnlChooseEndpoints.setFirstComponent(treeEndPointA);
        pnlChooseEndpoints.setSecondComponent(treeEndPointB);
            
        return pnlChooseEndpoints;
    }

    @Override
    public boolean onAdvance() {
        boolean advance = treeEndPointA.getValue() != null && treeEndPointB.getValue() != null;
        if (!advance)
            NotificationsUtil.showError("Must select both endpoints");
        return advance;
    }

    @Override
    public boolean onBack() {
        return true;
    }
    
}
