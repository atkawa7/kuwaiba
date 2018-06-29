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
package org.kuwaiba.web.procmanager;

import com.vaadin.data.HasValue;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Render the current activity and all activities of a process instance
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessInstanceView extends HorizontalSplitPanel {
    private final RemoteProcessDefinition processDefinition;
    private RemoteProcessInstance processInstance;
    private final HashMap<RemoteActivityDefinition, Button> activities;
    
    private final WebserviceBean wsBean;
    private final RemoteSession remoteSession;
    
    private RemoteArtifactDefinition artifactDefinition;
    private RemoteArtifact artifact;
    
    private ArtifactView artifactView;
    
    private VerticalLayout activitiesLayout = new VerticalLayout();
    
    public ProcessInstanceView(RemoteProcessInstance processInstance, RemoteProcessDefinition processDefinition, WebserviceBean wsBean, RemoteSession remoteSession) {
        setStyleName("processmanager");
        addStyleName("activitylist");
        setSizeFull();
        this.wsBean = wsBean;
        this.remoteSession = remoteSession;
        
        this.processDefinition = processDefinition;
        this.processInstance = processInstance;
        activities = new HashMap();
                        
        setSplitPosition(20, Unit.PERCENTAGE);
        initView();
    }
    
    private boolean actorEnabled(RemoteActor actor) {
        try {
            List<GroupInfoLight> groups = wsBean.getGroupsForUser(
                    remoteSession.getUserId(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    remoteSession.getSessionId());
            
            if (actor != null) {
                
                for (GroupInfoLight group : groups) {

                    if (actor.getName().equals(group.getName()))
                        return true;
                }
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        return false;
    }
            
    private void renderActivityButton(VerticalLayout activitiesLayout, RemoteActivityDefinition nextActivity) {
        
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
        
    private void renderArtifact(RemoteActivityDefinition currentActivity) {
        try {
            artifactDefinition = wsBean.getArtifactDefinitionForActivity(
                processDefinition.getId(),
                currentActivity.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }

        if (artifactDefinition != null) {

            try {
                artifact = wsBean.getArtifactForActivity(
                    processInstance.getId(),
                    currentActivity.getId(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    remoteSession.getSessionId());
            } catch (ServerSideException ex) {
                artifact = null;
                //NotificationsUtil.showError(ex.getMessage());
            }
        }
        
        if (actorEnabled(currentActivity.getActor())) {
            
            VerticalLayout artifactWrapperLayout = new VerticalLayout();
            artifactWrapperLayout.setHeight("100%");
            artifactWrapperLayout.setStyleName("formmanager");

            VerticalLayout artifactContainer = new VerticalLayout();
            artifactContainer.setSizeFull();
            
            HorizontalLayout secondHorizontalLayout = new HorizontalLayout();
            secondHorizontalLayout.setSpacing(false);
            secondHorizontalLayout.setSizeFull();
            Button btnSave = new Button(artifact == null ? "OK" : "Update");

            if (!actorEnabled(currentActivity.getActor()))
                btnSave.setEnabled(false);

            btnSave.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    RemoteArtifact remoteArtifact = null;

                    try {
                        remoteArtifact = wsBean.getArtifactForActivity(
                            processInstance.getId(),
                            currentActivity.getId(),
                            Page.getCurrent().getWebBrowser().getAddress(),
                            remoteSession.getSessionId());

                        try {
                            remoteArtifact.setContent(artifactView.getArtifactRenderer().getContent());
                        } catch (Exception ex) {
                            Notification.show("Error", ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                        remoteArtifact.setSharedInformation(artifactView.getArtifactRenderer().getSharedInformation());

                    } catch (ServerSideException ex) {
                        byte[] content;
                        try {
                            content = artifactView.getArtifactRenderer().getContent();
                        } catch (Exception ex1) {
                            Notification.show("Error", ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                        
                        remoteArtifact = new RemoteArtifact(
                            ProcessCache.artifactCounter++, 
                            "", 
                            "", 
                            content, 
                            artifactView.getArtifactRenderer().getSharedInformation());
                    }

                    try {
                        if (artifact == null) {

                            wsBean.commitActivity(
                                    processInstance.getId(),
                                    currentActivity.getId(),
                                    remoteArtifact,
                                    Page.getCurrent().getWebBrowser().getAddress(),
                                    remoteSession.getSessionId());
                            
                            processInstance = wsBean.getProcessInstance(
                                processInstance.getId(), 
                                Page.getCurrent().getWebBrowser().getAddress(),
                                remoteSession.getSessionId());
                            
                            updateActivities();
                            
                        } else {

                            wsBean.updateActivity(
                                    processInstance.getId(),
                                    currentActivity.getId(),
                                    remoteArtifact,
                                    Page.getCurrent().getWebBrowser().getAddress(),
                                    remoteSession.getSessionId());

                            Notification.show("Success", "The artifact was updated", Notification.Type.TRAY_NOTIFICATION);

                        }
                        processInstance = wsBean.getProcessInstance(
                            processInstance.getId(), 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            remoteSession.getSessionId());
                        
                    } catch (ServerSideException ex) {
                        Notifications.showError(ex.getMessage());
                    }
                }
            });
            
            Button btnViewProcessInstance = new Button("View");
            btnViewProcessInstance.setDescription("View Process Instance");
            btnViewProcessInstance.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    
                    ProcessGraph processGraph = new ProcessGraph(
                        processInstance, 
                        processDefinition, 
                        wsBean, 
                        remoteSession);
                    Window newWindow = new Window();
                    newWindow.setWidth(80, Unit.PERCENTAGE);
                    newWindow.setHeight(80, Unit.PERCENTAGE);
                    newWindow.setModal(true);
                    newWindow.setContent(processGraph);
                    getUI().addWindow(newWindow);
                }
            });
            
            GridLayout gl = new GridLayout();
            gl.setSizeFull();
            gl.setColumns(2);
            gl.setRows(1);
            gl.addComponent(btnSave);
            gl.addComponent(btnViewProcessInstance);

            gl.setComponentAlignment(btnViewProcessInstance, Alignment.MIDDLE_LEFT);
            gl.setComponentAlignment(btnSave, Alignment.MIDDLE_RIGHT);
            
            secondHorizontalLayout.addComponent(gl);
            secondHorizontalLayout.setComponentAlignment(gl, Alignment.MIDDLE_CENTER);
                        
            Panel pnlArtifact = new Panel();
            pnlArtifact.setSizeFull();
            pnlArtifact.setContent(artifactView = new ArtifactView(artifactDefinition, artifact, wsBean, remoteSession, processInstance));
            
            artifactContainer.addComponent(pnlArtifact);
            artifactContainer.addComponent(secondHorizontalLayout);
            
            artifactContainer.setExpandRatio(pnlArtifact, 9f);
            artifactContainer.setExpandRatio(secondHorizontalLayout, 1f);
            
            if (true) {
                VerticalLayout artifactPanel = new VerticalLayout();
                artifactPanel.setSizeFull();
                                
                VerticalLayout artifactTools = new VerticalLayout();
                artifactTools.setWidth(100, Unit.PERCENTAGE);
                
                CheckBox chkIdleActivity = new CheckBox("Idle Activity");
                boolean idleActivity = false;
                
                if (artifact != null) {
                    for (StringPair pair : artifact.getSharedInformation()) {
                        if (pair.getKey().equals("__idle__")) {
                            idleActivity = Boolean.valueOf(pair.getValue());
                            artifactView.getArtifactRenderer().getSharedMap().put("__idle__", pair.getValue());
                            break;
                        }
                    }
                }
                chkIdleActivity.setValue(idleActivity);
                
                chkIdleActivity.addValueChangeListener(new HasValue.ValueChangeListener() {
                    @Override
                    public void valueChange(HasValue.ValueChangeEvent event) {
                        artifactView.getArtifactRenderer().getSharedMap().put("__idle__", event.getValue().toString());
                        Notification.show(event.getValue().toString());
                    }
                });
                artifactTools.addComponent(chkIdleActivity);
                
                artifactPanel.addComponent(artifactTools);
                artifactPanel.addComponent(artifactContainer);
                
                artifactPanel.setExpandRatio(artifactTools, 0.1f);
                artifactPanel.setExpandRatio(artifactContainer, 9.9f);
                
                artifactWrapperLayout.addComponent(artifactPanel);
                
            } else {
                
                artifactWrapperLayout.addComponent(artifactContainer);                
            }
            
            ProcessInstanceView.this.setSecondComponent(artifactWrapperLayout);
        } else {
            Notifications.showError("His Role does not allow to start this activity");
        }
    }
    
    public void initView() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setStyleName("activitylist");
        wrapper.setHeight("100%");
        
        activitiesLayout = new VerticalLayout();
        activitiesLayout.setSpacing(false);
        wrapper.addComponent(activitiesLayout);
        
        updateActivities();
        setFirstComponent(wrapper);
    }
    
    private void updateActivities() {
        activities.clear();
        activitiesLayout.removeAllComponents();
        
        try {
            List<RemoteActivityDefinition> lstActivities = wsBean.getProcessInstanceActivitiesPath(
                processInstance.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
            
            for (RemoteActivityDefinition activity : lstActivities)
                renderActivityButton(activitiesLayout, activity);      
            
            if (lstActivities != null && !lstActivities.isEmpty()) {

                activities.get(lstActivities.get(lstActivities.size() - 1)).addStyleName("activity-current");
                renderArtifact(lstActivities.get(lstActivities.size() - 1));
            }
            
        } catch (ServerSideException ex) {
            
            Notifications.showError(ex.getMessage());
        }
    }
    
}
