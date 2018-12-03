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
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ContainerInformationStep  {

//    private final NewContainerWizard newContainerWizard;
//    private FormLayout content;
//    private String containerName;
//    private RemoteClassMetadataLight containerClass;
//    private RemoteObjectLight containerTemplate;
//    private boolean useTemplate;
//    
//    public ContainerInformationStep(NewContainerWizard newContainerWizard) {
//        this.newContainerWizard = newContainerWizard;
//    }
//    
//    public String getContainerName() {
//        return containerName;
//    }
//    
//    public RemoteClassMetadataLight getContainerClass() {
//        return containerClass;        
//    }
//    
//    public RemoteObjectLight getContainerTemplate() {
//        return containerTemplate;
//    }
//    
//    public boolean isUseTemplate() {
//        return useTemplate;
//    }
//
//    @Override
//    public String getCaption() {
//        return "Container Information";
//    }
//
//    @Override
//    public Component getContent() {
//        if (content == null) {
//            content = new FormLayout();
//            content.setMargin(true);
//            content.setSizeFull();
//            
//            TextField txtContainerName = new TextField("Container Name");
//            
//            txtContainerName.setWidth(StyleOsp.FIELD_WIDTH);
//            txtContainerName.setHeight(StyleOsp.FIELD_HEIGHT);
//            
//            txtContainerName.setRequiredIndicatorVisible(true);
//            
//            content.addComponent(txtContainerName);
//            
//            WebserviceBeanLocal wsBean = newContainerWizard.getParentComponent().getWsBean();
//            String ipAddress = Page.getCurrent().getWebBrowser().getAddress();
//            String sessionId = newContainerWizard.getParentComponent().getApplicationSession().getSessionId();
//            
//            List<RemoteClassMetadataLight> containerClasses;
//            try {
//                containerClasses = wsBean.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false, ipAddress, sessionId);
//            } catch (ServerSideException ex) {
//                NotificationsUtil.showError(ex.getMessage());
//                return new VerticalLayout();
//            }
//                        
//            ComboBox<RemoteClassMetadataLight> cmbContainerClass = new ComboBox<>("Container Class", containerClasses);
//            
//            cmbContainerClass.setWidth(StyleOsp.FIELD_WIDTH);
//            cmbContainerClass.setHeight(StyleOsp.FIELD_HEIGHT);
//            cmbContainerClass.setEmptySelectionAllowed(false);
//            cmbContainerClass.setRequiredIndicatorVisible(true);
//            
//            content.addComponent(cmbContainerClass);
//            
//            ComboBox<RemoteObjectLight> cmbContainerTemplate = new ComboBox<>("Container Template");
//            
//            cmbContainerTemplate.setWidth(StyleOsp.FIELD_WIDTH);
//            cmbContainerTemplate.setHeight(StyleOsp.FIELD_HEIGHT);
//                        
//            cmbContainerTemplate.setEnabled(false);
//                        
//            content.addComponent(cmbContainerTemplate);
//            
//            CheckBox chkUseTemplate = new CheckBox("Do not use any template");
//            
//            chkUseTemplate.setWidth(StyleOsp.FIELD_WIDTH);
//            chkUseTemplate.setHeight(StyleOsp.FIELD_HEIGHT);
//            
//            content.addComponent(chkUseTemplate);
//            
//            cmbContainerTemplate.addValueChangeListener((event) -> {
//                try {
//                        RemoteClassMetadataLight selectedContainerClass = (RemoteClassMetadataLight) cmbContainerClass.getValue();
//                        
//                        List<RemoteObjectLight> templates = wsBean.getTemplatesForClass(selectedContainerClass.getClassName(), ipAddress, sessionId);
//                        
//                        cmbContainerTemplate.clear();
//                        cmbContainerTemplate.setItems(templates);
//                        cmbContainerTemplate.setEnabled(true);
//                        cmbContainerTemplate.setEmptySelectionAllowed(false);
//                        
//                    } catch (ServerSideException ex) {
//                        NotificationsUtil.showError(ex.getMessage());
//                    }
//            });     
//        }
//        return content;
//    }
//
//    @Override
//    public boolean onAdvance() {
//        containerName = ((TextField) content.getComponent(0)).getValue();
//        containerClass = (RemoteClassMetadataLight) ((NativeSelect) content.getComponent(1)).getValue();
//        
//        if (containerName == null || containerClass == null) {
//            NotificationsUtil.showError("There are mandatory fields *");
//            return false;
//        }
//        
//        containerTemplate = (RemoteObjectLight) ((NativeSelect) content.getComponent(2)).getValue();
//        useTemplate = !((CheckBox) content.getComponent(3)).getValue();
//        
//        if (useTemplate && containerTemplate == null) {
//            NotificationsUtil.showError("Select a template");
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onBack() {
//        return true;
//    }
    
}
