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

import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.google.overlays.ConnectionPolyline;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

/**
 * Wizard to guide the creation of physical connections
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PhysicalConnectionWizard extends Window implements 
        WizardProgressListener {
    public static int SELECTED_FINISH_BUTTON = 0;
    public static int SELECTED_CANCEL_BUTTON = 1;
    
    private int selectedButton = SELECTED_CANCEL_BUTTON;
    private TopComponent parentComponent;
    private Wizard wizard = null;   
    
    private ConnectionPolyline connection;
    private PhysicalConnectionConfiguration connConfig;
    
    public PhysicalConnectionWizard(TopComponent parentComponent, ConnectionPolyline connection) {
        super("Physical Connection Wizard");
        center();
        
        this.parentComponent = parentComponent;
        this.connection = connection;
        this.connConfig = new PhysicalConnectionConfiguration();
        
        initWizard();
        setHeight("70%");
        setWidth("70%");
        setModal(true);
        setClosable(false);
        setContent(wizard);
    }
    
    private void initWizard() {
        wizard = new Wizard();
        wizard.setUriFragmentEnabled(true);
        wizard.addStep(new FirstStepChooseEndpoint(this), "first");
        wizard.addStep(new SecondStepConnectionSettings(this), "second");
        wizard.setSizeFull();
        wizard.addListener(this);
    }
    
    public ConnectionPolyline getConnection() {
        return connection;
    }
    
    public PhysicalConnectionConfiguration getConnectionConfiguration() {
        return connConfig;
    }
    
    public Wizard getWizard() {
        return wizard;
    }

    @Override
    public void activeStepChanged(WizardStepActivationEvent event) {
    }

    @Override
    public void stepSetChanged(WizardStepSetChangedEvent event) {
    }

    @Override
    public void wizardCompleted(WizardCompletedEvent event) {
        connection.setCaption(connConfig.getCaption());
        connection.setStrokeColor(connConfig.getStrokeColor());
        connection.setStrokeOpacity(connConfig.getStrokeOpacity());
        connection.setStrokeWeight(connConfig.getStrokeWeight());
                
        RemoteObjectLight aRbo = connection.getSource().getRemoteObjectLight();
        RemoteObjectLight bRbo = connection.getTarget().getRemoteObjectLight();
                
        String [] names = null;
        String [][] values = null;
        
        String name = connConfig.getCaption(); // connection name
        
        long typeOid = connConfig.getTypeOid();
        if (typeOid == 0) {
            names = new String[]{"name"};
            values = new String[][]{new String[]{name}};
        }
        else {
            String type = Long.toString(connConfig.getTypeOid());
            
            names = new String[]{"name", "type"};
            values = new String[][]{new String[]{name}, new String[]{type}};
        }
        RemoteObject commonParent = null;
        long connectionId = -1L;
        
        String connectionClass = connConfig.getConnectionClass();
        String errorMessage = "";
        
        WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
        String ipAddress = getUI().getPage().getWebBrowser().getAddress();
        String sessioId = getTopComponent().getApplicationSession().getSessionId();
        
        try {        
            commonParent = wsBean.getCommonParent(aRbo.getClassName(), aRbo.getOid(), bRbo.getClassName(), bRbo.getOid(), ipAddress, sessioId);
            if (commonParent != null)
                connectionId = wsBean.createPhysicalConnection(aRbo.getClassName(), aRbo.getOid(), bRbo.getClassName(), bRbo.getOid(), commonParent.getClassName(), commonParent.getOid(), names, values, connectionClass, ipAddress, sessioId);
            else {
                Notification.show(
                        "Failed create Physical Connection" 
                                + "\n\nThe common parent between " + aRbo.toString() + " and " + bRbo.toString() + " is the Navigation Tree Root" 
                                + "\n\nThe Navigation Tree Root never will be the parent for one connection", 
                        Notification.Type.ERROR_MESSAGE);
                return;                
            }
        } catch (ServerSideException ex) {
            errorMessage = ex.getMessage();
        }   
        connection.setId(connectionId);
        
        if (connectionId != -1L) {                        
            int numberOfChildren = connConfig.getNumChildren();
            if (numberOfChildren > 0) {
                String childrenType = connConfig.getPortType();
                try {
                    wsBean.createBulkPhysicalConnections(childrenType, numberOfChildren, connectionClass, connectionId, ipAddress, sessioId);
                    Notification.show("Children connections were created successfully", Notification.Type.HUMANIZED_MESSAGE);
                } catch (ServerSideException ex) {
                    errorMessage = ex.getMessage();
                    NotificationsUtil.showError(errorMessage);
                }
            }
            RemoteObjectLight connInfo = new RemoteObjectLight(connection.getId(), name, connectionClass);
            connection.setConnectionInfo(connInfo);
            
            Notification.show("The object was created successfully", Notification.Type.HUMANIZED_MESSAGE);
            selectedButton = SELECTED_FINISH_BUTTON;
            close();
        }
        else
            NotificationsUtil.showError(errorMessage);
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent event) {
        selectedButton = SELECTED_CANCEL_BUTTON;
        close();
    }
    
    public int getSelectedButton() {
        return selectedButton;
    }
    
    public TopComponent getTopComponent() {
        return parentComponent;
    }    
}
