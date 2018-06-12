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
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.vaadin.teemu.wizards.WizardStep;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ContainerInformationStep implements WizardStep {
    private final NewContainerWizard newContainerWizard;
    private FormLayout content;
    private String containerName;
    private RemoteClassMetadataLight containerClass;
    private RemoteObjectLight containerTemplate;
    
    public ContainerInformationStep(NewContainerWizard newContainerWizard) {
        this.newContainerWizard = newContainerWizard;
    }
    
    public String getContainerName() {
        return containerName;
    }
    
    public RemoteClassMetadataLight getContainerClass() {
        return containerClass;        
    }
    
    public RemoteObjectLight getContainerTemplate() {
        return containerTemplate;
    }

    @Override
    public String getCaption() {
        return "Container Information";
    }

    @Override
    public Component getContent() {
        if (content == null) {
            content = new FormLayout();
            content.setMargin(true);
            content.setSizeFull();
            
            TextField txtcontainerName = new TextField("Container Name");
            txtcontainerName.setRequiredIndicatorVisible(true);
            content.addComponent(txtcontainerName);
            
            WebserviceBeanLocal wsBean = newContainerWizard.getParentComponent().getWsBean();
            String ipAddress = Page.getCurrent().getWebBrowser().getAddress();
            String sessionId = newContainerWizard.getParentComponent().getApplicationSession().getSessionId();
            
            List<RemoteClassMetadataLight> containerClasses;
            try {
                containerClasses = wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false, ipAddress, sessionId);
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
                return new VerticalLayout();
            }
            NativeSelect txtContainerClass = new NativeSelect("Container Class");
            txtContainerClass.setEmptySelectionCaption("None");            

            
            for (RemoteClassMetadataLight containerClass : containerClasses) {
                //txtContainerClass.addItem(containerClass);
                //txtContainerClass.setItemCaption(containerClass, containerClass.toString());
            }
            content.addComponent(txtContainerClass);
            
            NativeSelect txtContainerTemplate = new NativeSelect("Container Template");
            txtContainerTemplate.setEmptySelectionCaption("None");
            content.addComponent(txtContainerTemplate);
            
//            txtContainerClass.addListener(new Property.ValueChangeListener() {
//                @Override
//                public void valueChange(Property.ValueChangeEvent event) {
//                    try {
//                        List<RemoteObjectLight> templates = wsBean.getTemplatesForClass(containerName, ipAddress, sessionId);
//                        txtContainerTemplate.addItem(this);
//                        /*
//                        for ()
//                        txtContainerTemplate.setItemCaption(this, containerName);.addItem(this);
//                        */
//                    } catch (ServerSideException ex) {
//                        NotificationsUtil.showError(ex.getMessage());
//                    }
//                }
//            });
            
        }
        return content;
    }

    @Override
    public boolean onAdvance() {
        containerName = ((TextField) content.getComponent(0)).getValue();
        containerClass = (RemoteClassMetadataLight) ((NativeSelect) content.getComponent(1)).getValue();
        containerTemplate = (RemoteObjectLight) ((NativeSelect) content.getComponent(2)).getValue();
        return true;
    }

    @Override
    public boolean onBack() {
        return true;
    }
    
}
