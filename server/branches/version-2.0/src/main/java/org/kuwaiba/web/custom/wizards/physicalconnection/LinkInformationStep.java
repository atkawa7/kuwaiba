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
public class LinkInformationStep implements WizardStep {
    private final NewLinkWizard newLinkWizard;
    private FormLayout content;
    private String linkName;
    private ClassInfoLight linkClass;
    private RemoteObjectLight linkTemplate;
    private boolean useTemplate;
    
    public LinkInformationStep(NewLinkWizard newLinkWizard) {
        this.newLinkWizard = newLinkWizard;
    }
    
    public String getLinkName() {
        return linkName;
    }
    
    public ClassInfoLight getLinkClass() {
        return linkClass;        
    }
    
    public RemoteObjectLight getLinkTemplate() {
        return linkTemplate;
    }
    
    public boolean isUseTemplate() {
        return useTemplate;
    }

    @Override
    public String getCaption() {
        return "Link Information";
    }

    @Override
    public Component getContent() {
        if (content == null) {
            content = new FormLayout();
            content.setMargin(true);
            content.setSizeFull();
            
            TextField txtLinkName = new TextField("Link Name");
            
            txtLinkName.setWidth(StyleOsp.FIELD_WIDTH);
            txtLinkName.setHeight(StyleOsp.FIELD_HEIGHT);
            
            txtLinkName.setRequired(true);
            
            content.addComponent(txtLinkName);
            
            WebserviceBeanLocal wsBean = newLinkWizard.getParentComponent().getWsBean();
            String ipAddress = Page.getCurrent().getWebBrowser().getAddress();
            String sessionId = newLinkWizard.getParentComponent().getApplicationSession().getSessionId();
            
            List<ClassInfoLight> linkClasses;
            try {
                linkClasses = wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALLINK, false, false, ipAddress, sessionId);
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
                return new VerticalLayout();
            }
            NativeSelect txtLinkClass = new NativeSelect("Link Class");
            
            txtLinkClass.setWidth(StyleOsp.FIELD_WIDTH);
            txtLinkClass.setHeight(StyleOsp.FIELD_HEIGHT);
            
            txtLinkClass.setRequired(true);
            
            content.addComponent(txtLinkClass);
            
            for (ClassInfoLight linkClass : linkClasses) {
                txtLinkClass.addItem(linkClass);
                txtLinkClass.setItemCaption(linkClass, linkClass.toString());
            }
            txtLinkClass.setNullSelectionAllowed(false);
            
            
            NativeSelect txtLinkTemplate = new NativeSelect("Link Template");
            
            txtLinkTemplate.setWidth(StyleOsp.FIELD_WIDTH);
            txtLinkTemplate.setHeight(StyleOsp.FIELD_HEIGHT);
                        
            txtLinkTemplate.setEnabled(false);
            
            content.addComponent(txtLinkTemplate);
            
            CheckBox chkUseTemplate = new CheckBox("Do not use any template");
            
            chkUseTemplate.setWidth(StyleOsp.FIELD_WIDTH);
            chkUseTemplate.setHeight(StyleOsp.FIELD_HEIGHT);
            
            content.addComponent(chkUseTemplate);
            
            txtLinkClass.addValueChangeListener(new Property.ValueChangeListener() {
                
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    try {
                        ClassInfoLight selectedLinkClass = (ClassInfoLight) txtLinkClass.getValue();
                        
                        List<RemoteObjectLight> templates = wsBean.getTemplatesForClass(selectedLinkClass.getClassName(), ipAddress, sessionId);
                        
                        for (RemoteObjectLight template : templates) {
                            txtLinkTemplate.addItem(template);
                            txtLinkTemplate.setItemCaption(template, template.toString());
                        }
                        txtLinkTemplate.setEnabled(true);
                        txtLinkTemplate.setNullSelectionAllowed(false);
                        
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
        linkName = ((TextField) content.getComponent(0)).getValue();
        linkClass = (ClassInfoLight) ((NativeSelect) content.getComponent(1)).getValue();
        
        if (linkName == null && linkClass == null) {
            NotificationsUtil.showError("There are mandatory fields *");
            return false;
        }
        
        linkTemplate = (RemoteObjectLight) ((NativeSelect) content.getComponent(2)).getValue();
        useTemplate = !((CheckBox) content.getComponent(3)).getValue();
        
        if (useTemplate && linkTemplate == null) {
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
