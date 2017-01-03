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
package org.kuwaiba.web.custom.wizards.physicalconnection;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.Validator;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.web.custom.tree.DynamicTree;
import org.vaadin.teemu.wizards.WizardStep;

/**
 * First Step Choose endpoint for Physical Connection Wizard
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FirstStep implements WizardStep {
    private PhysicalConnectionWizard wizard;
    private DynamicTree treeEndPointA;
    private DynamicTree treeEndPointB;
    
    VerticalLayout content;
    
    public FirstStep(PhysicalConnectionWizard wizard) {
        content = null;
        this.wizard = wizard;
    }
    
    @Override
    public String getCaption() {
        return "Choose endpoint";
    }
    
    private Component initTrees() {
        RemoteObjectLight rootSource = wizard.getConnection().getSource().getRemoteObjectLight();
        InventoryObjectNode rootNodeA = new InventoryObjectNode(rootSource);
        treeEndPointA = new DynamicTree(rootNodeA, wizard.getTopComponent());
        rootNodeA.setTree(treeEndPointA);
        
        RemoteObjectLight rootTarget = wizard.getConnection().getTarget().getRemoteObjectLight();
        InventoryObjectNode rootNodeB = new InventoryObjectNode(rootTarget);
        treeEndPointB = new DynamicTree(rootNodeB, wizard.getTopComponent());
        rootNodeB.setTree(treeEndPointB);
        
        HorizontalSplitPanel pnlChooseEndpoints = new HorizontalSplitPanel();
        pnlChooseEndpoints.setFirstComponent(treeEndPointA);
        pnlChooseEndpoints.setSecondComponent(treeEndPointB);
            
        return pnlChooseEndpoints;
    }
        
    @Override
    public Component getContent() {
        if (content == null)  {
            content = new VerticalLayout();
            content.setSizeFull();
            
            NativeSelect selectConnection = new NativeSelect("Connection Class");
            selectConnection.addItems(
                    "Wire Container",
                    "Wireless Container",
                    "Electrical Link",
                    "Optical Link",
                    "Wireless Link");

            selectConnection.addListener(new Property.ValueChangeListener() {

                @Override
                public void valueChange(Property.ValueChangeEvent event) {                                
                    String connectionClass = (String) event.getProperty().getValue();
                    
                    if (connectionClass != null) {
                        PhysicalConnectionConfiguration connConfig = wizard.getConnectionConfiguration();
                        connConfig.chooseWizardType(connectionClass);
                        
                        if (treeEndPointA == null && treeEndPointB == null) {
                            Component trees = initTrees();
                            
                            if (trees != null) {
                                content.addComponent(new Label(""
                                        + "<p>Choose endpoint</p>"
                                        + "<p>Select the objects (port or nodes) you'd like to connect.</p>", 
                                        ContentMode.HTML));
                                content.addComponent(trees);
                            }
                        }
                    }
                }
            });
            content.addComponent(selectConnection);
        }
        return content;
    }
    
    @Override
    public boolean onAdvance() {                
        if (treeEndPointA.getValue() != null && treeEndPointB.getValue() != null) {
            boolean advanced = true;
            
            if (treeEndPointA.getValue() instanceof InventoryObjectNode &&
                    treeEndPointB.getValue() instanceof InventoryObjectNode) {
                try {
                    InventoryObjectNode aObjectNode = (InventoryObjectNode) treeEndPointA.getValue();
                    InventoryObjectNode bObjectNode = (InventoryObjectNode) treeEndPointB.getValue();
                    
                    WebserviceBeanLocal wsBean = wizard.getTopComponent().getWsBean();
                    String ipAddress = wizard.getUI().getPage().getWebBrowser().getAddress();
                    String sessionId = wizard.getTopComponent().getApplicationSession().getSessionId();
                    //TODO: validate The port A or B is already connected
                    RemoteObjectLight aRemoteObject = (RemoteObjectLight) aObjectNode.getObject();
                    RemoteObjectLight bRemoteObject = (RemoteObjectLight) bObjectNode.getObject();

                    ClassInfo aClassInfo = wsBean.getClass(aRemoteObject.getClassName(), ipAddress, sessionId);
                    
                    HashMap<String, Integer> aValidators = new HashMap<>();
                    if (aClassInfo != null)
                        for (Validator validator : aClassInfo.getValidators())
                            aValidators.put(validator.getLabel(), validator.getValue());
                                    
                    ClassInfo bClassInfo = wsBean.getClass(bRemoteObject.getClassName(), ipAddress, sessionId);

                    HashMap<String, Integer> bValidators = new HashMap<>();
                    if (bClassInfo != null)
                        for (Validator validator : bClassInfo.getValidators())
                            bValidators.put(validator.getLabel(), validator.getValue());
                    
                    PhysicalConnectionConfiguration connConfig = wizard.getConnectionConfiguration();
                    switch(connConfig.getWizardType()) {
                        case PhysicalConnectionConfiguration.WIZARD_TYPE_CONTAINER:
                            int aValidatorPhysicalNodeValue =
                                    aValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_NODE) == null ? 0 :
                                    aValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_NODE);
                            
                            if (aValidatorPhysicalNodeValue == 1) {
                                int bValidatorPhysicalNodeValue = 
                                        bValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_NODE) == null ? 0 :
                                        bValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_NODE);
                                
                                if (!(bValidatorPhysicalNodeValue == 1)) {
                                    NotificationsUtil.showError("The object selected in the right tree cannot be connected using a container");
                                    advanced = false;
                                }
                            }
                            else {
                                NotificationsUtil.showError("The object selected in the left tree cannot be connected using a container");
                                advanced = false;
                            }
                        break;
                        case PhysicalConnectionConfiguration.WIZARD_TYPE_LINK:
                            int aValidatorPhysicalEndpoint = 
                                    aValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_ENDPOINT) == null ? 0 :
                                    aValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_ENDPOINT);
                            
                            if (aValidatorPhysicalEndpoint == 1) {
                                int bValidatorPhysicalEndpoint = 
                                        bValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_ENDPOINT) == null ? 0 :
                                        bValidators.get(PhysicalConnectionConfiguration.VALIDATOR_PHYSICAL_ENDPOINT);
                                
                                if (!(bValidatorPhysicalEndpoint == 1)) {
                                    NotificationsUtil.showError("The object selected in the right tree cannot be connected using a link");
                                    advanced = false;
                                }
                            }
                            else {
                                NotificationsUtil.showError("The object selected in the left tree cannot be connected using a link");
                                advanced = false;
                            }
                        break;
                        default:
                            advanced = false;
                    }
                } catch (ServerSideException ex) {
                    NotificationsUtil.showError(ex.getMessage());
                    advanced = false;
                }
            }
            else { // when select the dummy node
                NotificationsUtil.showError("You have to select both sides of this connection");
                advanced = false;
            }
            return advanced;
        }
        else {
            NotificationsUtil.showError("You have to select both sides of this connection");
            return false;
        }
    }
    
    @Override
    public boolean onBack() {
        return true;
    }
}
