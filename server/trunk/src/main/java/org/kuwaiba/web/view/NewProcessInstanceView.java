/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.view;

import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.elements.FormLoader;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteForm;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NewProcessInstanceView extends HorizontalSplitPanel implements Button.ClickListener {
    private RemoteProcessDefinition processDefinition;
    private RemoteProcessInstance processInstance;
    private HashMap<RemoteActivityDefinition, Button> activities;
    
    private final WebserviceBeanLocal wsBean;
    
    public NewProcessInstanceView(RemoteProcessInstance processInstance, RemoteProcessDefinition processDefinition, WebserviceBeanLocal wsBean) {
        setStyleName("processmanager");
        addStyleName("activitylist");
        setSizeFull();
        this.wsBean = wsBean;
        this.processDefinition = processDefinition;
        this.processInstance = processInstance;
        activities = new HashMap();
                        
        setSplitPosition(20, Unit.PERCENTAGE);
        initView();
    }
    
    @Override
    public void buttonClick(Button.ClickEvent event) {
        
        RemoteActivityDefinition nextActivity = processDefinition.getStartAction();
        
        while (nextActivity != null) {
            
            if (processInstance.getCurrentActivity() == nextActivity.getId()) {
                //TODO:Process to save activity
                if (nextActivity.getNextActivity() != null) {
                    processInstance.setCurrentActivity(nextActivity.getNextActivity().getId());
                    activities.get(nextActivity.getNextActivity()).addStyleName("activity-current");
                    if (nextActivity != null)
                        renderArtifact(nextActivity.getNextActivity());
                                            
                    return;
                }
            }
            nextActivity = nextActivity.getNextActivity();
        }
        if (nextActivity == null) {
            try {
                 List<RemoteProcessInstance>  processInstances = wsBean.getProcessInstances(
                        processDefinition.getId(),
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) getSession().getAttribute("session")).getSessionId());
                
                ((MainView) getUI().getContent()).setSecondComponent(new ProcessInstancesView(processDefinition, processInstances, wsBean));
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
            
        }
    }
        
    private void render(VerticalLayout activitiesLayout, RemoteActivityDefinition nextActivity) {
        
        Button btnActivity = new Button(nextActivity.getName());
        btnActivity.setStyleName("activity");
        btnActivity.setWidth("100%");
        activitiesLayout.addComponent(btnActivity);
        activitiesLayout.setComponentAlignment(btnActivity,  Alignment.TOP_CENTER);

        btnActivity.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (nextActivity != null)
                    renderArtifact(nextActivity);
            }
        });   
        
        activities.put(nextActivity, btnActivity);
    }
    
    private void renderArtifact(RemoteActivityDefinition nextActivity) {
        VerticalLayout artifactWrapperLayout = new VerticalLayout();
        artifactWrapperLayout.setHeight("100%");
        artifactWrapperLayout.setStyleName("formmanager");

        VerticalSplitPanel artifactContainer = new VerticalSplitPanel();
        artifactContainer.setSizeFull();
        artifactContainer.setSplitPosition(91, Unit.PERCENTAGE);


        Panel artifactDefContainer = new Panel();
        artifactDefContainer.setStyleName("formmanager");
        artifactDefContainer.setSizeFull();
//        artifactDefContainer.setContent(getFormRenderer(nextActivity));                

        VerticalLayout secondVerticalLayout = new VerticalLayout();
        secondVerticalLayout.setSizeFull();
        Button btnSave = new Button("Save");
        btnSave.addClickListener(NewProcessInstanceView.this);
        secondVerticalLayout.addComponent(btnSave);
        secondVerticalLayout.setComponentAlignment(btnSave, Alignment.MIDDLE_CENTER);

        artifactContainer.setFirstComponent(artifactDefContainer);
        artifactContainer.setSecondComponent(secondVerticalLayout);

        artifactWrapperLayout.addComponent(artifactContainer);

        NewProcessInstanceView.this.setSecondComponent(artifactWrapperLayout);
        
    }
    
    public void initView() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setStyleName("activitylist");
        wrapper.setHeight("100%");
        
        VerticalLayout activitiesLayout = new VerticalLayout();
        activitiesLayout.setSpacing(false);
        wrapper.addComponent(activitiesLayout);
        
        RemoteActivityDefinition nextActivity = processDefinition.getStartAction();
        
        while (nextActivity != null) {
                        
            render(activitiesLayout, nextActivity);
                                    
            nextActivity = nextActivity.getNextActivity();
        }
        nextActivity = processDefinition.getStartAction();
        
        while (nextActivity != null) {
            
            if (processInstance.getCurrentActivity() == nextActivity.getId()) {
                activities.get(nextActivity).addStyleName("activity-current");
//                if (nextActivity != null)
//                    renderArtifact(nextActivity);
            }
            nextActivity = nextActivity.getNextActivity();
        }
        setFirstComponent(wrapper);
    }      
    /*
    private FormRenderer getFormRenderer(RemoteActivityDefinition activityDefinition) {
        String address = Page.getCurrent().getWebBrowser().getAddress();
        
        RemoteSession remoteSession = (RemoteSession) getSession().getAttribute("session");
        
        RemoteForm remoteForm = null;
        
        if (activityDefinition.getId() == 1) {
            try {
                remoteForm = wsBean.getForm(50479, address, remoteSession.getSessionId());
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        } else if (activityDefinition.getId() == 2) {
            try {
                remoteForm = wsBean.getForm(39750, address, remoteSession.getSessionId());
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        } else if (activityDefinition.getId() == 3) {
            try {
                remoteForm = wsBean.getForm(39770, address, remoteSession.getSessionId());
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        } else if (activityDefinition.getId() == 4) {
            try {
                remoteForm = wsBean.getForm(39790, address, remoteSession.getSessionId());
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        } else if (activityDefinition.getId() == 5) {
            try {
                remoteForm = wsBean.getForm(50499, address, remoteSession.getSessionId());
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
        }
        if (remoteForm != null) {
            
            if (remoteForm.getStructure() == null)
                return null;

            FormLoader formBuilder = new FormLoader(remoteForm.getStructure());            
            formBuilder.build();

            FormRenderer formRenderer = new FormRenderer(formBuilder);
            formRenderer.render(wsBean, remoteSession);

            return formRenderer;
        }
        return null;
    }
    */
}
