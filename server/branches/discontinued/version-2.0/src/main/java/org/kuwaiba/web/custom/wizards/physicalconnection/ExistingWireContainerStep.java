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

import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.vaadin.teemu.wizards.WizardStep;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ExistingWireContainerStep implements WizardStep {
    private final NewLinkWizard newLinkWizard;
    private DynamicTree treeWireContainer;     
    private CheckBox chkNoUseTemplate;
    
    private VerticalLayout content;
    
    public ExistingWireContainerStep(NewLinkWizard newLinkWizard) {
        this.newLinkWizard = newLinkWizard;
    }

    @Override
    public String getCaption() {
        return "Existing Wire Container";
    }
    
    public RemoteObjectLight getWireContainer() {
        return treeWireContainer != null && treeWireContainer.getValue() instanceof InventoryObjectNode && ((InventoryObjectNode) treeWireContainer.getValue()).getObject() instanceof RemoteObjectLight ? 
            (RemoteObjectLight) ((InventoryObjectNode) treeWireContainer.getValue()).getObject() : null;
    }
    
    public boolean isUseWireContainer() {
        return chkNoUseTemplate != null ? !chkNoUseTemplate.getValue() : false;
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
        WebserviceBeanLocal wsBean = newLinkWizard.getParentComponent().getWsBean();
        String ipAddress = Page.getCurrent().getWebBrowser().getAddress();
        String sessionId = newLinkWizard.getParentComponent().getApplicationSession().getSessionId();
        
        RemoteObjectLight source = newLinkWizard.getConnectionPolyline().getSource().getRemoteObjectLight();
        RemoteObjectLight target = newLinkWizard.getConnectionPolyline().getSource().getRemoteObjectLight();
        
        try {
            RemoteObjectLight commonParent = wsBean.getCommonParent(source.getClassName(), source.getOid(), target.getClassName(), target.getOid(), ipAddress, sessionId);
            
            if (commonParent.getOid() == -1L) {
                NotificationsUtil.showError("Can not create connection whose common parent is root of hierarchy");
                return new VerticalLayout();
            }
            List<RemoteObjectLight> existingWireContainersList = wsBean.getContainersBetweenObjects(source.getClassName(), source.getOid(), 
                target.getClassName(), target.getOid(), Constants.CLASS_WIRECONTAINER, ipAddress, sessionId);
            
            WireContainerRootNode wireContainerRootNode = new WireContainerRootNode(Constants.CLASS_WIRECONTAINER, existingWireContainersList);
            treeWireContainer = new DynamicTree(wireContainerRootNode, newLinkWizard.getParentComponent());
            wireContainerRootNode.setTree(treeWireContainer);
            
            
            VerticalLayout verticalLayout = new VerticalLayout();
            
            Panel pnlTree = new Panel();                        
            pnlTree.setContent(treeWireContainer);
            pnlTree.setSizeFull();
            pnlTree.setHeight("250");
            
            verticalLayout.addComponent(pnlTree);
            
            chkNoUseTemplate = new CheckBox("Do not use any container");
            
            chkNoUseTemplate.addValueChangeListener(new Property.ValueChangeListener() {
                
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    
                    treeWireContainer.setEnabled(!chkNoUseTemplate.getValue());
                }
            });
            
            verticalLayout.addComponent(chkNoUseTemplate);
            
            return verticalLayout;
                       
        } catch (ServerSideException ex) {
            
            NotificationsUtil.showError(ex.getMessage());
            return new VerticalLayout();
        }        
    }

    @Override
    public boolean onAdvance() {
        if(!chkNoUseTemplate.getValue()) {
            boolean advance = treeWireContainer.getValue() != null;
            if (!advance)
                NotificationsUtil.showError("Must select a container");
            return advance;
        }
        return chkNoUseTemplate.getValue();
    }

    @Override
    public boolean onBack() {
        return true;
    }
    
}
