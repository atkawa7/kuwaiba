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

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import java.util.List;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
import org.vaadin.teemu.wizards.WizardStep;

/**
 * Second Step Connection settings for Physical Connection Wizard 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SecondStepConnectionSettings implements WizardStep {
    private PhysicalConnectionWizard wizard;
    private FormLayout content;
    
    public SecondStepConnectionSettings(PhysicalConnectionWizard wizard) {
        content = null;
        this.wizard = wizard;
    }
        
    @Override
    public String getCaption() {
        return "Connection Settings";
    }

    @Override
    public Component getContent() {
        if (content == null) {
            content = new FormLayout();
            content.setSizeFull();
            
            TextField tfConnectionName = new TextField("Connection Name");
            tfConnectionName.setRequiredIndicatorVisible(true);
            content.addComponent(tfConnectionName);
            
            try {
                PhysicalConnectionConfiguration connConfig = wizard.getConnectionConfiguration();
                                
                WebserviceBeanLocal wsBean = wizard.getTopComponent().getWsBean();
                String ipAddress = wizard.getUI().getPage().getWebBrowser().getAddress();
                String sessionId = wizard.getTopComponent().getApplicationSession().getSessionId();
                
                String connectionTypeClass = connConfig.getConnectionTypeClass();
                List<RemoteObjectLight> types = wsBean.getListTypeItems(connectionTypeClass, ipAddress, sessionId);
                
                NativeSelect selectConnectionTypeClass = new NativeSelect("Connection Type");
                selectConnectionTypeClass.setEmptySelectionCaption("None");
                
                if (types != null && types.size() > 0)
                    for (RemoteObjectLight type : types) {
                        //selectConnectionTypeClass.addItem(type);
                        //selectConnectionTypeClass.setItemCaption(type, type.getName());
                    }
                
                content.addComponent(selectConnectionTypeClass);
                
                int wizardType = connConfig.getWizardType();

                if (wizardType == PhysicalConnectionConfiguration.WIZARD_TYPE_CONTAINER) {
                    List<ClassInfoLight> portClasses = wsBean.getSubClassesLight("GenericPhysicalLink", false, false, ipAddress, sessionId);

                    if (portClasses.size() > 0) {
                        NativeSelect selectChildrenType = new NativeSelect("Children type");
                        selectChildrenType.setEmptySelectionCaption(null);
                        selectChildrenType.setEmptySelectionAllowed(false);
                        
                        
                        boolean flag = true;
                        for (ClassInfoLight portClass : portClasses) {
                            //selectChildrenType.addItem(portClass);
                            //selectChildrenType.setItemCaption(portClass, portClass.getClassName());                            
                            if (flag) {
                                selectChildrenType.setValue(portClass);
                                flag = false;
                            }
                        }
                        
                        content.addComponent(selectChildrenType);
                        
                        //IntStepper numChildren = new IntStepper("Number of children");
                        //numChildren.setMinValue(0);
                        //content.addComponent(numChildren);
                    }
                }
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        }
        return content;
    }

    @Override
    public boolean onAdvance() {
        int componentCount = content.getComponentCount();
        
        if (componentCount >= 2) {
            PhysicalConnectionConfiguration connConfig = wizard.getConnectionConfiguration();
            connConfig.setCaption(((TextField)content.getComponent(0)).getValue());
            
            RemoteObjectLight object = (RemoteObjectLight) 
                    ((NativeSelect) content.getComponent(1)).getValue();
            
            if (object != null)
                connConfig.setTypeOid(object.getOid());
            else
                connConfig.setTypeOid(0);
            
            if (componentCount == 4) {
                ClassInfoLight portClass = (ClassInfoLight) ((NativeSelect) content.getComponent(2)).getValue();
                connConfig.setPortType(portClass.getClassName());
                
                //int numChildren = ((IntStepper) content.getComponent(3)).getValue();
                //connConfig.setNumChildren(numChildren);
            }
            return true;
        }
        else {
            NotificationsUtil.showError("Select Connection Class");
            return false;
        }
    }

    @Override
    public boolean onBack() {
        return true;
    }
}