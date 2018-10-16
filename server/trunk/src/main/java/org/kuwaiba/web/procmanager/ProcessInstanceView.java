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
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.components.impl.PrintWindow;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.AbstractElementField;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.notifications.MessageBox;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
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
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConditionalActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;

/**
 * Renders the current activity and all activities of a process instance
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessInstanceView extends DynamicComponent {
    private final RemoteProcessDefinition processDefinition;
    private RemoteProcessInstance processInstance;
    private final HashMap<RemoteActivityDefinition, Button> activities;
    
    private final WebserviceBean wsBean;
    private final RemoteSession remoteSession;
    
    private RemoteArtifactDefinition artifactDefinition;
    private RemoteArtifact artifact;
    
    private ArtifactView artifactView;
    
    private VerticalLayout activitiesLayout = new VerticalLayout();
    /**
     * Debug mode flag
     */
    public boolean debugMode;
    
    
    public ProcessInstanceView(RemoteProcessInstance processInstance, RemoteProcessDefinition processDefinition, WebserviceBean wsBean, RemoteSession remoteSession) {
        
        debugMode = Boolean.valueOf(String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("debugMode")));
        
        setStyleName("processmanager");
        addStyleName("activitylist");
        setSizeFull();
        this.wsBean = wsBean;
        this.remoteSession = remoteSession;
        
        this.processDefinition = processDefinition;
        this.processInstance = processInstance;
        activities = new HashMap();
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
//        if (nextActivity.isIdling())
//            btnActivity.setIcon(VaadinIcons.THIN_SQUARE);
//        else
            btnActivity.setIcon(VaadinIcons.CHECK_SQUARE_O);
        
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
    
    private void setArtifact(RemoteActivityDefinition currentActivity, Button btnNext, Button eventBtn) {
        RemoteArtifact remoteArtifact;

        try {
            remoteArtifact = wsBean.getArtifactForActivity(
                processInstance.getId(),
                currentActivity.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());

            try {
                remoteArtifact.setContent(artifactView.getArtifactRenderer().getContent());
            } catch (Exception ex) {
                Notifications.showError(ex.getMessage());
                return;
            }
            remoteArtifact.setSharedInformation(artifactView.getArtifactRenderer().getSharedInformation());

        } catch (ServerSideException ex) {
            byte[] content;
            try {
                content = artifactView.getArtifactRenderer().getContent();
            } catch (Exception ex1) {
                Notifications.showError(ex1.getMessage());
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
            if (eventBtn.equals(btnNext)) {

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
                
                if (currentActivity instanceof RemoteConditionalActivityDefinition)
                    updateActivities();                                        

                Notifications.showInfo("The activity was updated");

            }
            processInstance = wsBean.getProcessInstance(
                processInstance.getId(), 
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());

        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        
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
            return;
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
            
            Button btnSave = new Button(I18N.gm("save"));
            btnSave.setIcon(VaadinIcons.CHECK_CIRCLE);
                        
            Button btnNext = new Button(I18N.gm("next"));
            btnNext.setIcon(VaadinIcons.CHEVRON_CIRCLE_RIGHT);
            btnNext.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
            
            // Current Activity can be updated
            if (processInstance.getCurrentActivity() != currentActivity.getId()) {
                btnSave.setEnabled(false);
                btnNext.setEnabled(false);
            }
            // Only Idle Activities can be modified if the Selected Activity
            // are no equals to Process Instance Current Activity
            if (currentActivity.isIdling())
                btnSave.setEnabled(true);
            // If the activity is a conditional can update the value
            if (currentActivity instanceof RemoteConditionalActivityDefinition)
                btnSave.setEnabled(true);
            // The actor is authorized
            if (!actorEnabled(currentActivity.getActor())) {
                btnSave.setEnabled(false);
                btnNext.setEnabled(false);
            }

            Button.ClickListener clickListener = new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Button eventBtn = event.getButton();
                    
                    if (currentActivity.confirm()) {
                        
                        Label label = new Label("Are you sure you want to save this Activity?");
                        label.setIcon(VaadinIcons.QUESTION_CIRCLE_O);
                        
                        MessageBox.getInstance().showMessage(label).addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                
                                if (MessageBox.getInstance().continues())
                                    setArtifact(currentActivity, btnNext, eventBtn);
                            }
                        });
                    } else
                        setArtifact(currentActivity, btnNext, eventBtn);
                }
            };    
            btnSave.addClickListener(clickListener);
            btnNext.addClickListener(clickListener);
                                    
            Button btnViewProcessInstance = new Button(I18N.gm("view"));
            btnViewProcessInstance.setDescription("View Process Instance");
            btnViewProcessInstance.setIcon(VaadinIcons.SITEMAP);
            btnViewProcessInstance.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
            
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
            gl.setColumns(3);
            gl.setRows(1);
            gl.addComponent(btnSave);
            gl.addComponent(btnViewProcessInstance);
            gl.addComponent(btnNext);

            gl.setComponentAlignment(btnSave, Alignment.MIDDLE_RIGHT);
            gl.setComponentAlignment(btnViewProcessInstance, Alignment.MIDDLE_CENTER);
            gl.setComponentAlignment(btnNext, Alignment.MIDDLE_LEFT);
            
            secondHorizontalLayout.addComponent(gl);
            secondHorizontalLayout.setComponentAlignment(gl, Alignment.MIDDLE_CENTER);
                        
            Panel pnlArtifact = new Panel();
            pnlArtifact.setSizeFull();
            pnlArtifact.setContent(artifactView = new ArtifactView(artifactDefinition, artifact, wsBean, remoteSession, processInstance));
            
            artifactContainer.addComponent(pnlArtifact);
            artifactContainer.addComponent(secondHorizontalLayout);
            
            artifactContainer.setExpandRatio(pnlArtifact, 9f);
            artifactContainer.setExpandRatio(secondHorizontalLayout, 1f);
            
            boolean idleActivity = true;
            boolean interruptedActivity = false;

            if (artifact != null) {
                for (StringPair pair : artifact.getSharedInformation()) {

                    if (pair.getKey().equals("__idle__")) {
                        idleActivity = Boolean.valueOf(pair.getValue());
                        artifactView.getArtifactRenderer().getSharedMap().put("__idle__", pair.getValue());
                    }

                    if (pair.getKey().equals("__interrupted__")) {
                        interruptedActivity = Boolean.valueOf(pair.getValue());                            
                        artifactView.getArtifactRenderer().getSharedMap().put("__idle__", pair.getValue());
                    }
                }
            }
            
            if (currentActivity.isIdling() || interruptedActivity) {
                VerticalLayout artifactPanel = new VerticalLayout();
                artifactPanel.setSizeFull();
                                
                HorizontalLayout artifactTools = new HorizontalLayout();
                artifactTools.setWidth(100, Unit.PERCENTAGE);
                
                if (artifactDefinition.isPrintable()) {
                    Button btnPrint = new Button(I18N.gm("print"), VaadinIcons.PRINT);
                    btnPrint.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
                                        
                    btnPrint.addClickListener(new ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
                            
                            File file = new File(processEnginePath + "/form/templates/" + artifactDefinition.getPrintableTemplate());

                            byte[] byteTemplate = PrintWindow.getFileAsByteArray(file);
                            String stringTemplate = new String(byteTemplate);
                                                        
                            List<AbstractElement> elements = ((FormRenderer) artifactView.getContent()).getFormStructure().getElements();
                            
                            
                            for (AbstractElement element : elements) {

                                if (element instanceof AbstractElementField) {
                                    AbstractElementField elementField = (AbstractElementField) element;

                                    if (elementField.getId() != null) {
                                        String id = element.getId();

                                        String value = "";

                                        if (elementField.getValue() != null) {
                                            if (elementField.getValue() instanceof RemoteObjectLight) {

                                                value = ((RemoteObjectLight) elementField.getValue()).getName();
                                            }
                                            else {

                                                value = elementField.getValue().toString();
                                            }
                                        }
                                        stringTemplate = stringTemplate.replace("${" + id + "}", value);
                                    }
                                }
                            }
                            
                            final String TMP_FILE_PATH = processEnginePath + "/temp/processengine.tmp"; //NOI18N
                            try {
                                
                                PrintWriter templateInstance;
                                templateInstance = new PrintWriter(TMP_FILE_PATH);
                                templateInstance.println(stringTemplate);
                                templateInstance.close();
                                
                            } catch (FileNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            File tmpFile = new File(TMP_FILE_PATH);                            

                            byte[] tmpByteTemplate = PrintWindow.getFileAsByteArray(tmpFile);
                            
                            StreamResource fileStream = ResourceFactory.getFileStream(tmpByteTemplate, currentActivity.getName() + "_" + Calendar.getInstance().getTimeInMillis() + ".html");
                            fileStream.setMIMEType("text/html"); //NOI18N
                            setResource(String.valueOf(processInstance.getId()), fileStream);
                            ResourceReference rr = ResourceReference.create(fileStream, ProcessInstanceView.this, String.valueOf(processInstance.getId()));
                            Page.getCurrent().open(rr.getURL(), "Download Report", true);
                        }
                    });
                                        
                    artifactTools.addComponent(btnPrint);
                    artifactTools.setComponentAlignment(btnPrint, Alignment.MIDDLE_RIGHT);
                }
                
                if (interruptedActivity)
                    artifactTools.addComponent(new Label("Interrupted Activity"));
                                
                if (currentActivity.isIdling()) {
                    CheckBox chkIdleActivity = new CheckBox("Complete Activity Confirmation");
                    
                    if (!debugMode) {
                        
                        chkIdleActivity.setValue(!idleActivity);

                        chkIdleActivity.setEnabled(idleActivity);
                        btnSave.setEnabled(idleActivity);
                    }
                    chkIdleActivity.addValueChangeListener(new HasValue.ValueChangeListener() {
                        @Override
                        public void valueChange(HasValue.ValueChangeEvent event) {
                            artifactView.getArtifactRenderer().getSharedMap().put("__idle__", String.valueOf(!((Boolean) event.getValue())));
                        }
                    });

                    artifactTools.addComponent(chkIdleActivity);
                    artifactTools.setComponentAlignment(chkIdleActivity, Alignment.MIDDLE_LEFT);
                }
                
                artifactPanel.addComponent(artifactTools);
                artifactPanel.addComponent(artifactContainer);
                
                artifactPanel.setExpandRatio(artifactTools, 0.4f);
                artifactPanel.setExpandRatio(artifactContainer, 9.6f);
                
                artifactWrapperLayout.addComponent(artifactPanel);
                
            } else {
                
                artifactWrapperLayout.addComponent(artifactContainer);                
            }
            
            setComponentCenter(artifactWrapperLayout);
        }
        else {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(false);
            Label label = new Label("<h1 style=\"color:#ff8a80;\">The group you belong to can not start or edit this activity<h1>", ContentMode.HTML);            
            verticalLayout.addComponent(label);
            verticalLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
            verticalLayout.setSizeFull();
            setComponentCenter(verticalLayout);
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
        setComponentLeft(wrapper);
        initializeComponent();
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
                activities.get(lstActivities.get(lstActivities.size() - 1)).setIcon(VaadinIcons.THIN_SQUARE);
                activities.get(lstActivities.get(lstActivities.size() - 1)).addStyleName("activity-current");
                renderArtifact(lstActivities.get(lstActivities.size() - 1));
            }
            
        } catch (ServerSideException ex) {
            
            Notifications.showError(ex.getMessage());
        }
    }
       
}
