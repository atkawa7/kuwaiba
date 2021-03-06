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
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.Collections;
import java.util.List;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
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
    private ClassInfoLight containerClass;
    private RemoteObjectLight containerTemplate;
    private boolean useTemplate;
    
    public ContainerInformationStep(NewContainerWizard newContainerWizard) {
        this.newContainerWizard = newContainerWizard;
    }
    
    public String getContainerName() {
        return containerName;
    }
    
    public ClassInfoLight getContainerClass() {
        return containerClass;        
    }
    
    public RemoteObjectLight getContainerTemplate() {
        return containerTemplate;
    }
    
    public boolean isUseTemplate() {
        return useTemplate;
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
            
            TextField txtContainerName = new TextField("Container Name");
            
            txtContainerName.setWidth(StyleOsp.FIELD_WIDTH);
            txtContainerName.setHeight(StyleOsp.FIELD_HEIGHT);
            
            txtContainerName.setRequired(true);
            
            content.addComponent(txtContainerName);
            
            WebserviceBeanLocal wsBean = newContainerWizard.getParentComponent().getWsBean();
            String ipAddress = Page.getCurrent().getWebBrowser().getAddress();
            String sessionId = newContainerWizard.getParentComponent().getApplicationSession().getSessionId();
            
            List<ClassInfoLight> containerClasses;
            try {
                containerClasses = wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false, ipAddress, sessionId);
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
                return new VerticalLayout();
            }
                        
            NativeSelect txtContainerClass = new NativeSelect("Container Class");
            
            txtContainerClass.setWidth(StyleOsp.FIELD_WIDTH);
            txtContainerClass.setHeight(StyleOsp.FIELD_HEIGHT);
            
            txtContainerClass.setRequired(true);
            
            content.addComponent(txtContainerClass);
            
            for (ClassInfoLight aContainerClass : containerClasses) {
                txtContainerClass.addItem(aContainerClass);
                txtContainerClass.setItemCaption(aContainerClass, aContainerClass.toString());
            }
            txtContainerClass.setNullSelectionAllowed(false);
                        
            NativeSelect txtContainerTemplate = new NativeSelect("Container Template");
            
            txtContainerTemplate.setWidth(StyleOsp.FIELD_WIDTH);
            txtContainerTemplate.setHeight(StyleOsp.FIELD_HEIGHT);
                        
            txtContainerTemplate.setEnabled(false);
                        
            content.addComponent(txtContainerTemplate);
            
            CheckBox chkUseTemplate = new CheckBox("Do not use any template");
            
            chkUseTemplate.setWidth(StyleOsp.FIELD_WIDTH);
            chkUseTemplate.setHeight(StyleOsp.FIELD_HEIGHT);
            
            content.addComponent(chkUseTemplate);
            
            txtContainerClass.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    try {
                        ClassInfoLight selectedContainerClass = (ClassInfoLight) txtContainerClass.getValue();
                        
                        List<RemoteObjectLight> templates = wsBean.getTemplatesForClass(selectedContainerClass.getClassName(), ipAddress, sessionId);
                        
                        txtContainerTemplate.removeAllItems();
                        
                        for (RemoteObjectLight template : templates) {
                            txtContainerTemplate.addItem(template);
                            txtContainerTemplate.setItemCaption(template, template.toString());
                        }
                        txtContainerTemplate.setEnabled(true);
                        txtContainerTemplate.setNullSelectionAllowed(false);
                        
                    } catch (ServerSideException ex) {
                        NotificationsUtil.showError(ex.getMessage());
                    }
                }
            });           
        }
        return content;
    }

    @Override
    public boolean onAdvance() {
        containerName = ((TextField) content.getComponent(0)).getValue();
        containerClass = (ClassInfoLight) ((NativeSelect) content.getComponent(1)).getValue();
        
        if (containerName == null || containerClass == null) {
            NotificationsUtil.showError("There are mandatory fields *");
            return false;
        }
        
        containerTemplate = (RemoteObjectLight) ((NativeSelect) content.getComponent(2)).getValue();
        useTemplate = !((CheckBox) content.getComponent(3)).getValue();
        
        if (useTemplate && containerTemplate == null) {
            NotificationsUtil.showError("Select a template");
            return false;
        }
        return true;
    }

    @Override
    public boolean onBack() {
        return true;
    }
    
}
