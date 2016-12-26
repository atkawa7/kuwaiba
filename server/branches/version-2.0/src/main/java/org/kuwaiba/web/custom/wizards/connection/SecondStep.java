/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.wizards.connection;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.vaadin.teemu.wizards.WizardStep;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SecondStep implements WizardStep {
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    
    private PopupConnectionWizardView wizard;
    private DynamicTree treeEndPointA;
    private DynamicTree treeEndPointB;
    
    VerticalLayout content = new VerticalLayout();
    
    public SecondStep(PopupConnectionWizardView wizard) {
        mem = PersistenceService.getInstance().getMetadataEntityManager();
        this.wizard = wizard;
    }
    
    @Override
    public String getCaption() {
        return "Choose endpoint";
    }

    @Override
    public Component getContent() {        
        content.setSizeFull();
        content.setMargin(true);
        
        VerticalSplitPanel pnlStart = new VerticalSplitPanel();
        pnlStart.addComponent(new Label("<b>Choose endpoint</b>", ContentMode.HTML));
        pnlStart.addComponent(new Label("<p>Select the objects (port or nodes) you'd like to connect.</p>", ContentMode.HTML));
        
        HorizontalSplitPanel pnlChooseEndpoints = new HorizontalSplitPanel();
        RemoteBusinessObjectLight rootSource = wizard.getConnection().getSource().getRemoteBusinessObject();
        RemoteBusinessObjectLight rootTarget = wizard.getConnection().getTarget().getRemoteBusinessObject();
        //treeEndPointA = new DynamicTree(new ObjectNode(rootSource, treeEndPointA), "Select A endpoint");
        pnlChooseEndpoints.setFirstComponent(treeEndPointA);
        //treeEndPointB = new DynamicTree(new ObjectNode(rootTarget, treeEndPointB), "Select A endpoint");
        pnlChooseEndpoints.setSecondComponent(treeEndPointB);
        
        content.addComponents(pnlStart, pnlChooseEndpoints);

        return content;
    }
    
    @Override
    public boolean onAdvance() {
        InventoryObjectNode aObjectNode = (InventoryObjectNode) treeEndPointA.getValue();
        InventoryObjectNode bObjectNode = (InventoryObjectNode) treeEndPointB.getValue();
        
        String error = "";
        boolean advanced = true;
        
        if (aObjectNode == null || bObjectNode == null) {
            error = "You have to select both sides of this connection";
            advanced = false;
        } else {
            
        }
        VerticalSplitPanel pnlError = new VerticalSplitPanel();
        pnlError.addComponent(new Label("<p style=\"color:red;\">"+error+".</p>", ContentMode.HTML));
        
        content.addComponent(pnlError);
        
        return advanced;
    }
    
    @Override
    public boolean onBack() {
        return true;
    }
}
