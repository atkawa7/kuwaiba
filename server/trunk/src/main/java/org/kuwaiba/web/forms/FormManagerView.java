/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.forms;

import com.vaadin.cdi.CDIView;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteForm;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteFormInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * A simple form manager
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CDIView("formmanager")
public class FormManagerView extends CustomComponent implements View {
    
    public static String VIEW_NAME = "formmanager";
    
    @Inject
    private WebserviceBean wsBean;
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        String save = "Save";
        String update = "Update";
        
        Label lblForms = new Label("Forms");
        ComboBox cmbForms = new ComboBox();        
        
        Label lblName = new Label("Name");                        
        TextField txtName = new TextField();
        
        Label lblDescription = new Label("Description");
        TextField txtDescription = new TextField();
        
        Label lblScript = new Label("Script");
        TextField txtScript = new TextField();
        
        Button btnSave = new Button(save);
        Button btnView = new Button("View");
        
        GridLayout gridLayout = new GridLayout();
        gridLayout.setColumns(2);
        
        gridLayout.addComponents(
            lblForms, cmbForms, 
            lblName, txtName, 
            lblDescription, txtDescription, 
            lblScript, txtScript, 
            btnView, btnSave);
        
        String address = Page.getCurrent().getWebBrowser().getAddress();
        getSession().setAttribute("wsBean", wsBean);
        RemoteSession remoteSession = (RemoteSession) getSession().getAttribute("session");
        
        try {
            List<RemoteForm> forms = wsBean.getForms(address, remoteSession.getSessionId());
            cmbForms.setItems(forms);
        } catch (ServerSideException ex) {
            Notification.show("The form can not be loaded", "The form can not be loaded", Notification.Type.ERROR_MESSAGE);
        }
        cmbForms.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                Object value = event.getValue();
                if (value != null && value instanceof RemoteForm) {
                    
                    txtName.setValue(((RemoteForm) value).getName());
                    txtDescription.setValue(((RemoteForm) value).getDescription());
                    txtScript.setValue(new String(((RemoteForm) value).getStructure()));
                    btnSave.setCaption(update);
                    
                } else {
                    
                    txtName.setValue("");
                    txtDescription.setValue("");
                    txtScript.setValue("");
                    btnSave.setCaption(save);
                }
            }
        });
        
        btnSave.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (save.equals(btnSave.getCaption())) {
                    
                    try {
                        wsBean.createForm(
                            txtName.getValue(), 
                            txtDescription.getValue(), 
                            txtScript.getValue().getBytes(), 
                            address, 
                            remoteSession.getSessionId());
                        
                        Notification.show("Form Saved", "Form Saved", Notification.Type.HUMANIZED_MESSAGE);
                        
                    } catch (ServerSideException ex) {
                        
                        Notification.show("Form Save Failed", "Form Save Failed", Notification.Type.ERROR_MESSAGE);
                    }
                    
                } else if (update.equals(btnSave.getCaption())) {
                    
                    try {
                        
                        wsBean.updateForm(
                                ((RemoteForm) cmbForms.getValue()).getId(),
                                txtName.getValue(),
                                txtDescription.getValue(),
                                txtScript.getValue().getBytes(),
                                address,
                                remoteSession.getSessionId());                        
                                                
                    } catch (ServerSideException ex) {
                        
                        Notification.show("Error on Update", "Error on Update", Notification.Type.ERROR_MESSAGE);
                    }
                }
                try {
                    List<RemoteForm> forms = wsBean.getForms(address, remoteSession.getSessionId());
                    cmbForms.setItems(forms);
                    
                } catch (ServerSideException ex) {
                    Notification.show("The form can not be loaded", "The forms can not be loaded", Notification.Type.ERROR_MESSAGE);
                }
            }
        });
        
        btnView.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                getSession().setAttribute("currentform", cmbForms.getValue());
                getUI().getNavigator().navigateTo(FormView.VIEW_NAME);
            }
        });
                
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(gridLayout);
        
        HorizontalLayout horizontalLayout = new HorizontalLayout();        
        Label lblFormInstances = new Label("Form Instances");
        ComboBox cmbFormInstances = new ComboBox();
        Button btnOpenFormInstance = new Button("Open");
        
        try {
            
            List<RemoteFormInstance> formInstances = wsBean.getFormInstances(address, remoteSession.getSessionId());
            cmbFormInstances.setItems(formInstances);
            
        } catch (ServerSideException ex) {
            Notification.show("The form instances can not be loaded", "The form instances can not be loaded", Notification.Type.ERROR_MESSAGE);
        }
        
        btnOpenFormInstance.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                getSession().setAttribute("currentforminstance", cmbFormInstances.getValue());
                getUI().getNavigator().navigateTo(FormInstanceView.VIEW_NAME);
            }
        });
        
        horizontalLayout.addComponents(lblFormInstances, cmbFormInstances, btnOpenFormInstance);
                
        verticalLayout.addComponent(horizontalLayout);
                
        setCompositionRoot(verticalLayout);
    }
}
