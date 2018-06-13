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
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessInstanceView extends HorizontalSplitPanel {
    private final RemoteProcessDefinition processDefinition;
    private RemoteProcessInstance processInstance;
    private final HashMap<RemoteActivityDefinition, Button> activities;
    
    private final WebserviceBeanLocal wsBean;
    private final RemoteSession remoteSession;
    
    private RemoteArtifactDefinition artifactDefinition;
    private RemoteArtifact artifact;
    
    private ArtifactView artifactView;
    
    private VerticalLayout activitiesLayout = new VerticalLayout();
    
    public ProcessInstanceView(RemoteProcessInstance processInstance, RemoteProcessDefinition processDefinition, WebserviceBeanLocal wsBean, RemoteSession remoteSession) {
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
            NotificationsUtil.showError(ex.getMessage());
        }
        return false;
    }
    /*        
    @Override
    public void buttonClick(Button.ClickEvent event) {
                        
        RemoteActivityDefinition nextActivity = processDefinition.getStartActivity();
        
        while (nextActivity != null) {
            
            if (processInstance.getCurrentActivity() == nextActivity.getId()) {
                try {
                    RemoteActor actor = nextActivity.getActor();
                                                            
                    if (actor != null && actor.getType() == Actor.TYPE_GROUP && actorEnabled(actor)) {
                        
                        if (artifactView != null && artifactView.getArtifactRenderer() != null) {
                            RemoteArtifact remoteArtifact = null;
                            
                            try {
                                remoteArtifact = wsBean.getArtifactForActivity(
                                    processInstance.getId(),
                                    nextActivity.getId(),
                                    Page.getCurrent().getWebBrowser().getAddress(),
                                    remoteSession.getSessionId());

                                remoteArtifact.setContent(artifactView.getArtifactRenderer().getContent());
                                remoteArtifact.setSharedInformation(artifactView.getArtifactRenderer().getSharedInformation());

                            } catch (ServerSideException ex) {
                                remoteArtifact = new RemoteArtifact(
                                    ProcessTest.artifactCounter++, 
                                    "", 
                                    "", 
                                    artifactView.getArtifactRenderer().getContent(), 
                                    artifactView.getArtifactRenderer().getSharedInformation());
                            }
                        
                            wsBean.commitActivity(
                                processInstance.getId(),
                                nextActivity.getId(),
                                remoteArtifact,
                                Page.getCurrent().getWebBrowser().getAddress(),
                                ((RemoteSession) getSession().getAttribute("session")).getSessionId());

                            processInstance = wsBean.getProcessInstance(
                                processInstance.getId(), 
                                Page.getCurrent().getWebBrowser().getAddress(),
                                ((RemoteSession) getSession().getAttribute("session")).getSessionId());
                        }
                    
                    } else {
                        NotificationsUtil.showError("The User are not authorized to commit the current activity");
                        return;
                    }
                    
                } catch (ServerSideException ex) {
                    NotificationsUtil.showError(ex.getMessage());
                }
                
                if (nextActivity.getNextActivity() != null) {
                    
                    activities.get(nextActivity.getNextActivity()).addStyleName("activity-current");
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
                
                ((MainView) getUI().getContent()).setSecondComponent(new ProcessInstancesView(processDefinition, processInstances, wsBean, ((RemoteSession) getSession().getAttribute("session"))));
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }
            int i = 0;
        }
    }
    */
        
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
    
////    private List<RemoteArtifact> getRemoteArtifacts() {
////        RemoteActivityDefinition activity = processDefinition.getStartActivity();
////        
////        List<RemoteArtifact> res = new ArrayList();
////        
////        while (activity != null) {
////            try {
////                RemoteArtifact remoteArtifact = wsBean.getArtifactForActivity(
////                        processInstance.getId(),
////                        activity.getId(),
////                        Page.getCurrent().getWebBrowser().getAddress(),
////                        remoteSession.getSessionId());
////                
////                res.add(remoteArtifact);
////                
////            } catch (ServerSideException ex) {
////            }
////            activity = activity.getNextActivity();
////        }
////        return res;
////    }
    
    
    private void renderArtifact(RemoteActivityDefinition currentActivity) {
        try {
            artifactDefinition = wsBean.getArtifactDefinitionForActivity(
                processDefinition.getId(),
                currentActivity.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
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

            VerticalSplitPanel artifactContainer = new VerticalSplitPanel();
            artifactContainer.setSizeFull();
            artifactContainer.setSplitPosition(91, Unit.PERCENTAGE);

            VerticalLayout secondVerticalLayout = new VerticalLayout();
            secondVerticalLayout.setSizeFull();
            Button btnSave = new Button(artifact == null ? "Save" : "Update");

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

                        remoteArtifact.setContent(artifactView.getArtifactRenderer().getContent());
                        remoteArtifact.setSharedInformation(artifactView.getArtifactRenderer().getSharedInformation());

                    } catch (ServerSideException ex) {
                        remoteArtifact = new RemoteArtifact(
                            ProcessCache.artifactCounter++, 
                            "", 
                            "", 
                            artifactView.getArtifactRenderer().getContent(), 
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
                            
////                            RemoteActivityDefinition nextActivity = wsBean.getNextActivityForProcessInstance(
////                                processInstance.getId(), 
////                                Page.getCurrent().getWebBrowser().getAddress(), 
////                                remoteSession.getSessionId());
////                            try {
////                                List<RemoteProcessInstance>  processInstances = wsBean.getProcessInstances(
////                                    processDefinition.getId(),
////                                    Page.getCurrent().getWebBrowser().getAddress(),
////                                    remoteSession.getSessionId());
////
////                                ((MainView) getUI().getContent()).setSecondComponent(new ProcessInstancesView(processDefinition, processInstances, wsBean, remoteSession));
////                            } catch (ServerSideException ex) {
////                                NotificationsUtil.showError(ex.getMessage());
////                            }
                            
////                            if (currentActivity.getNextActivity() != null) {
////
////                                activities.get(currentActivity.getNextActivity()).addStyleName("activity-current");
////                                renderArtifact(currentActivity.getNextActivity());
////                                                                
////                                Notification.show("Success", "The artifact was created", Notification.Type.TRAY_NOTIFICATION);
////                                
////                            } else {
////                                
////                            }
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
                        NotificationsUtil.showError(ex.getMessage());
                    }
                }
            });
            secondVerticalLayout.addComponent(btnSave);
            secondVerticalLayout.setComponentAlignment(btnSave, Alignment.MIDDLE_CENTER);

            artifactContainer.setFirstComponent(artifactView = new ArtifactView(artifactDefinition, artifact, wsBean, remoteSession, processInstance));
            artifactContainer.setSecondComponent(secondVerticalLayout);

            artifactWrapperLayout.addComponent(artifactContainer);

            ProcessInstanceView.this.setSecondComponent(artifactWrapperLayout);
        } else {
            NotificationsUtil.showError("His Role does not allow to start this activity");
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
////        RemoteActivityDefinition nextActivity = processDefinition.getStartActivity();
////        RemoteActivityDefinition currentActivity = nextActivity;        
////        
////        while (nextActivity != null) {
////            if (nextActivity.getId() == processInstance.getCurrentActivity())
////                currentActivity = nextActivity;
////                        
////            renderActivityButton(activitiesLayout, nextActivity);
////                                    
////            nextActivity = nextActivity.getNextActivity();
////        }
////        nextActivity = processDefinition.getStartActivity();
////        
////        if (currentActivity != null) {
////            
////            activities.get(currentActivity).addStyleName("activity-current");
////            renderArtifact(currentActivity);
////        }
        setFirstComponent(wrapper);
    }
    
    private void updateActivities() {
        activities.clear();
        activitiesLayout.removeAllComponents();
        
        try {
////            RemoteActivityDefinition activityDefinition = wsBean.getNextActivityForProcessInstance(
////                processInstance.getId(),
////                Page.getCurrent().getWebBrowser().getAddress(),
////                remoteSession.getSessionId());
////            
////            List<RemoteActivityDefinition> lstActivities = new ArrayList();
            
            List<RemoteActivityDefinition> lstActivities = wsBean.getProcessInstanceActivitiesPath(
                processInstance.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
            
////            activityDefinition = activityDefinition.getPreviousActivity();
////            RemoteActivityDefinition currentActivity = activityDefinition;
////            
////            while (activityDefinition != null) {
////                
////                if (activityDefinition.getId() == processInstance.getCurrentActivity())
////                    currentActivity = activityDefinition;
////                                
////                lstActivities.add(0, activityDefinition);
////                
////                activityDefinition = activityDefinition.getPreviousActivity();
////            }

            for (RemoteActivityDefinition activity : lstActivities)
                renderActivityButton(activitiesLayout, activity);      
            
            if (lstActivities != null && !lstActivities.isEmpty()) {

                activities.get(lstActivities.get(lstActivities.size() - 1)).addStyleName("activity-current");
                renderArtifact(lstActivities.get(lstActivities.size() - 1));
            }
            
        } catch (ServerSideException ex) {
            
            NotificationsUtil.showError(ex.getMessage());
        }
    }
    
}
