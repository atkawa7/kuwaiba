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

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import java.awt.Color;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.web.modules.osp.providers.google.overlays.ConnectionPolyline;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

/**
 * Wizard to guide the creation of physical connections using containers
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NewContainerWizard extends Window implements WizardProgressListener, WizardInterface {
    private final ConnectionPolyline connectionPolyline;
    private final TopComponent parentComponent;
    private boolean wizardCompleted = false;
    
    private final ContainerInformationStep containerInformationStep;
    private final ContainerEndpointsStep containerEndpointsStep;
            
    public NewContainerWizard(TopComponent parentComponent, ConnectionPolyline connectionPolyline) {
        super("New Container");
        center();
        this.connectionPolyline = connectionPolyline;
        this.parentComponent = parentComponent;
        
        Wizard wizard = new Wizard();
        wizard.setUriFragmentEnabled(true);
        wizard.addStep(containerInformationStep = new ContainerInformationStep(this), "containerInformationStep"); //NOI18N
        wizard.addStep(containerEndpointsStep = new ContainerEndpointsStep(this), "containerEndpointsStep"); //NOI18N
        wizard.setSizeFull();
        wizard.addListener(this);
        
        setHeight("70%");
        setWidth("70%");
        setModal(true);
        setClosable(false);
        setContent(wizard);
    }    
    
    public ConnectionPolyline getConnectionPolyline() {
        return connectionPolyline;
    }
    
    public TopComponent getParentComponent() {
        return parentComponent;
    }
    
    @Override
    public boolean isWizardCompleted() {
        return wizardCompleted;
    }

    @Override
    public void activeStepChanged(WizardStepActivationEvent event) {
        //Nothing to do
    }

    @Override
    public void stepSetChanged(WizardStepSetChangedEvent event) {
        //Nothing to do
    }

    @Override
    public void wizardCompleted(WizardCompletedEvent event) {        
//        String connectionName = containerInformationStep.getContainerName();
//        String connectionClassName = containerInformationStep.getContainerClass().getClassName();        
//        
//        InventoryObjectNode aObjectNode = (InventoryObjectNode) containerEndpointsStep.getTreeEndPointA().getValue();
//        InventoryObjectNode bObjectNode = (InventoryObjectNode) containerEndpointsStep.getTreeEndPointA().getValue();
//        
//        RemoteObjectLight endpointA = (RemoteObjectLight) aObjectNode.getObject();
//        RemoteObjectLight endpointB = (RemoteObjectLight) bObjectNode.getObject();
//        RemoteObjectLight template = new RemoteObjectLight(-1, null, null);
//        
//        WebserviceBeanLocal wsBean = getParentComponent().getWsBean();
//        String ipAddress = Page.getCurrent().getWebBrowser().getAddress();
//        String sessionId = getParentComponent().getApplicationSession().getSessionId();
//        
//        RemoteObjectLight parent;
//        try {
//            parent = wsBean.getCommonParent(endpointA.getClassName(), endpointA.getOid(), endpointB.getClassName(), endpointB.getOid(), ipAddress, sessionId);
//        } catch (ServerSideException ex) {
//            NotificationsUtil.showError(ex.getMessage());
//            wizardCompleted = false;
//            return;
//        }
//        
//        long connectionId = -1;
//        try {
//            connectionId = wsBean.createPhysicalConnection(endpointA.getClassName(), endpointA.getOid(), endpointB.getClassName(), endpointB.getOid(), parent.getClassName(), parent.getOid(), connectionName, connectionClassName, template.getOid(), ipAddress, sessionId);
//        } catch (ServerSideException ex) {
//            NotificationsUtil.showError(ex.getMessage());
//            wizardCompleted = false;
//            return;
//        }
//        
//        if (connectionId != -1l) {
//            ClassInfo connectionClass = null;
//            try {
//                connectionClass = wsBean.getClass(connectionClassName, ipAddress, sessionId);
//            } catch (ServerSideException ex) {
//                NotificationsUtil.showError(ex.getMessage());
//            }
//            
//            connectionPolyline.setId(connectionId);
//            
//            
//            connectionPolyline.setStrokeColor(toHexString(new Color(connectionClass.getColor())));
//            connectionPolyline.setStrokeOpacity(1);
//            connectionPolyline.setStrokeWeight(3);
//            
//            connectionPolyline.setConnectionInfo(new RemoteObjectLight(connectionId, connectionName, connectionClassName));
//            Notification.show("The connection was created successfully", Notification.Type.HUMANIZED_MESSAGE);
//            wizardCompleted = true;
//            close();
//        }
    }
    
    public String toHexString(Color colour) throws NullPointerException {
        // Thanks to: http://www.javacreed.com/how-to-get-the-hex-value-from-color/
        String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
        if (hexColour.length() < 6)
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        return "#" + hexColour;
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent event) {
        wizardCompleted = false;
    }
    
}
