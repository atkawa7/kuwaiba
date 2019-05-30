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
import com.vaadin.server.Resource;
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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.ScriptQueryExecutorImpl;
import org.kuwaiba.apis.forms.components.impl.ComponentUpload;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.AbstractElementField;
import org.kuwaiba.apis.forms.elements.ElementGrid;
import org.kuwaiba.apis.forms.elements.ElementScript;
import org.kuwaiba.apis.forms.elements.ElementUpload;
import org.kuwaiba.apis.forms.elements.FormDefinitionLoader;
import org.kuwaiba.apis.forms.elements.FunctionRunner;
import org.kuwaiba.apis.forms.elements.Runner;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.ParallelActivityDefinition;
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
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteParallelActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;

/**
 * Renders the current activity and all activities of a process instance
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
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
    private final boolean debugMode;
    
    private Button buttonClicked;
    private Resource buttonClickedResource;
    
    private final List<RemoteActivityDefinition> paths = new ArrayList();
    
    public ProcessInstanceView(RemoteProcessInstance processInstance, RemoteProcessDefinition processDefinition, WebserviceBean wsBean, RemoteSession remoteSession) {
        
        debugMode = Boolean.valueOf(String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("debugMode")));
        
        setStyleName("processmanager");
        setSizeFull();
        this.wsBean = wsBean;
        this.remoteSession = remoteSession;
        this.processDefinition = processDefinition;
        this.processInstance = processInstance;
        activities = new HashMap();
        initView();
    }
    
    private boolean actorEnabled(RemoteActor actor) {    
        if (actor == null)
            return true;
        
        try {
            List<GroupInfoLight> groups = wsBean.getGroupsForUser(
                remoteSession.getUserId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
                            
            for (GroupInfoLight group : groups) {
                if (actor.getName().equals(group.getName()))
                    return true;
            }                
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        return false;
    }
            
    private void renderActivityButton(VerticalLayout activitiesLayout, RemoteActivityDefinition nextActivity) {
        
        Button btnActivity = new Button(nextActivity.getName());
        btnActivity.addStyleName("activity");
        btnActivity.setWidth("100%");
        
        activitiesLayout.addComponent(btnActivity);
        activitiesLayout.setComponentAlignment(btnActivity,  Alignment.TOP_CENTER);

        btnActivity.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (event.getButton() != null && nextActivity != null) {
                    if (buttonClicked != null) {
                        buttonClicked.setIcon(buttonClickedResource);
                    }
                    buttonClicked = event.getButton();
                    buttonClickedResource = event.getButton().getIcon();
                    event.getButton().setIcon(VaadinIcons.CURSOR_O);
                    
                    renderArtifact(nextActivity);
                }
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
                //<editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes, write the XML artifact to a file">
//        try {
//            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/artifact" + artifactView.getId() + ".xml");
//            fos.write(artifactView.getArtifactRenderer().getContent());
//            fos.close();
//        } catch(Exception e) {}
        //</editor-fold>
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
                UUID.randomUUID().toString(), 
                "", 
                "", 
                content, 
                artifactView.getArtifactRenderer().getSharedInformation(), 
                new Date().getTime(),
                new Date().getTime()
            );
        }

        try {
            if (eventBtn.equals(btnNext)) {
                    wsBean.updateActivity(
                            processInstance.getId(),
                            currentActivity.getId(),
                            remoteArtifact,
                            Page.getCurrent().getWebBrowser().getAddress(),
                            remoteSession.getSessionId());
                
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
                    
                    if (artifactDefinition.getPostconditionsScript() != null) {
                        ScriptQueryExecutorImpl scriptQueryExecutorImpl = new ScriptQueryExecutorImpl(wsBean, remoteSession, processInstance);
                        String script = new String(artifactDefinition.getPostconditionsScript());
                        ElementScript elementScript = FormDefinitionLoader.loadExternalScripts(artifactDefinition.getExternalScripts());
                        FunctionRunner functionRunner = new FunctionRunner("postconditions", null, script, elementScript);
                        if (elementScript != null) {
                            for (Runner runner : elementScript.getFunctions().values())
                                runner.setScriptQueryExecutor(scriptQueryExecutorImpl);
                        }
                        functionRunner.setScriptQueryExecutor(scriptQueryExecutorImpl);
                        functionRunner.setParametersNames(Arrays.asList("processInstanceId", "activityDefinitionId", "nextActivityDefinitionId", "printableTemplateInstance"));

                        byte[] printableTemplateInstance = null;
                        if (artifactDefinition.isPrintable())
                            printableTemplateInstance = getPrintableTemplateInstanceAsByteArray(artifactDefinition, artifactView);

                        functionRunner.run(Arrays.asList(
                                processInstance.getId(), 
                                currentActivity.getId(), 
                                currentActivity.getNextActivity() != null ? currentActivity.getNextActivity().getId() : null, 
                                printableTemplateInstance));
                    }
                    updateActivities(-1);
            } else {
                wsBean.updateActivity(
                        processInstance.getId(),
                        currentActivity.getId(),
                        remoteArtifact,
                        Page.getCurrent().getWebBrowser().getAddress(),
                        remoteSession.getSessionId());
                if (artifactDefinition.getPostconditionsScript() != null) {
                    ScriptQueryExecutorImpl scriptQueryExecutorImpl = new ScriptQueryExecutorImpl(wsBean, remoteSession, processInstance);
                    String script = new String(artifactDefinition.getPostconditionsScript());
                    ElementScript elementScript = FormDefinitionLoader.loadExternalScripts(artifactDefinition.getExternalScripts());
                    FunctionRunner functionRunner = new FunctionRunner("postconditions", null, script, elementScript);
                    if (elementScript != null) {
                        for (Runner runner : elementScript.getFunctions().values())
                            runner.setScriptQueryExecutor(scriptQueryExecutorImpl);
                    }
                    functionRunner.setScriptQueryExecutor(scriptQueryExecutorImpl);
                    functionRunner.setParametersNames(Arrays.asList("processInstanceId", "activityDefinitionId", "nextActivityDefinitionId", "printableTemplateInstance"));

                    byte[] printableTemplateInstance = null;
                    if (artifactDefinition.isPrintable())
                        getPrintableTemplateInstanceAsByteArray(artifactDefinition, artifactView);

                    functionRunner.run(Arrays.asList(
                        processInstance.getId(), 
                        currentActivity.getId(), 
                        currentActivity.getNextActivity() != null ? currentActivity.getNextActivity().getId() : null, 
                        printableTemplateInstance));
                }
                updateActivities(currentActivity.getId());
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
            if (artifact == null && !enableActivity(currentActivity.getId())) {
                VerticalLayout verticalLayout = new VerticalLayout();
                verticalLayout.setSpacing(false);
                Label label = new Label("<h1 style=\"color:#ff8a80;\">The previous activity must be finished in order to start a new one<h1>", ContentMode.HTML);            
                verticalLayout.addComponent(label);
                verticalLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
                verticalLayout.setSizeFull();
                setComponentCenter(verticalLayout);
                return;                                                                                                
            }            
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
            if (currentActivity.getType() == ActivityDefinition.TYPE_END) {
                btnNext.setEnabled(false);                    
            }
            
            Button.ClickListener saveOrNextClickListener = new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Button eventBtn = event.getButton();
                    
                    if (currentActivity.confirm()) {
                        
                        Label label;
                        if (currentActivity instanceof RemoteConditionalActivityDefinition)
                            label = new Label("Are you sure you want to continue?");
                        else
                            label = new Label("Are you sure you want to save this activity?");
                                                                        
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
            btnSave.addClickListener(saveOrNextClickListener);
            btnNext.addClickListener(saveOrNextClickListener);
                                    
            Button btnViewProcessInstance = new Button(I18N.gm("view"));
            btnViewProcessInstance.setDescription("View Process Instance");
            btnViewProcessInstance.setIcon(VaadinIcons.SITEMAP);
            btnViewProcessInstance.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
            
            btnViewProcessInstance.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    
                    ProcessFlowchart processGraph = new ProcessFlowchart(
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
            pnlArtifact.setContent(artifactView = new ArtifactView(currentActivity, artifactDefinition, artifact, wsBean, remoteSession, processInstance));
            
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
            
            if (artifactDefinition.isPrintable() || currentActivity.isIdling() || interruptedActivity) {
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
                            byte[] tmpByteTemplate = getPrintableTemplateInstanceAsByteArray(artifactDefinition, artifactView);
                            
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
                    chkIdleActivity.setValue(!idleActivity);
                    
                    if (!debugMode) {
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
    
    public byte[] getPrintableTemplateInstanceAsByteArray(RemoteArtifactDefinition artifactDefinition, ArtifactView artifactView) {
        String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
        File file = new File(processEnginePath + "/form/templates/" + artifactDefinition.getPrintableTemplate());
        
        byte[] byteTemplate = getFileAsByteArray(file);
        String stringTemplate = new String(byteTemplate);

        List<AbstractElement> elements = ((FormRenderer) artifactView.getContent()).getFormStructure().getElements();

        for (AbstractElement element : elements) {

            if (element instanceof ElementGrid) {

                ElementGrid elementGrid = (ElementGrid) element;
                String id = elementGrid.getId();

                int columnsSize = elementGrid.getColums() != null ? elementGrid.getColums().size() : 0;

                if (elementGrid.getRows() != null) {
                    List<List<Object>> rows = elementGrid.getRows();
                    for (int i = 0; i < rows.size(); i++) {
                        List row = rows.get(i);

                        for (int j = 0; j < columnsSize; j++) {
                            String value = null;

                            if (j < row.size()) {
                                if (row.get(j) instanceof RemoteObjectLight)
                                    value = ((RemoteObjectLight) row.get(j)).getName();
                                else
                                    value = row.get(j).toString();
                            }
                            stringTemplate = stringTemplate.replace("${" + id + i + j + "}", value != null ? value : "");
                        }
                    }
                }
                if (elementGrid.getRows() == null || 
                    (elementGrid.getRows() != null && elementGrid.getRows().isEmpty())) {

                    for (int j = 0; j < columnsSize; j += 1)
                        stringTemplate = stringTemplate.replace("${" + id + "0" + j + "}", "");
                }
            }
            else if (element instanceof AbstractElementField) {
                AbstractElementField elementField = (AbstractElementField) element;

                if (elementField.getId() != null) {
                    if (elementField instanceof ElementUpload) {
                        ElementUpload elementUpload = (ElementUpload) elementField;
                        if (elementUpload.getElementEventListener() instanceof ComponentUpload) {
                            ComponentUpload componentUpload = (ComponentUpload) elementUpload.getElementEventListener();
                            stringTemplate = stringTemplate.replace(
                                "${" + element.getId() + "}", 
                                componentUpload.getUploadUrl());
                        }
                    } else {
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
        return getFileAsByteArray(tmpFile);
    }
    
    private byte[] getFileAsByteArray(File file) {
        try {
            Scanner in = new Scanner(file);

            String line = "";

            while (in.hasNext())
                line += in.nextLine();

            byte [] structure = line.getBytes();

            in.close();

            return structure;

        } catch (FileNotFoundException ex) {

            return null;
        }
    }
    
    public void initView() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setStyleName("activitylist");
        wrapper.setSizeFull();
        
        activitiesLayout = new VerticalLayout();
        activitiesLayout.setSpacing(false);
        
        Panel panel = new Panel();
        panel.setStyleName("activitylist");
        panel.setSizeFull();        
        wrapper.addComponent(panel);
        wrapper.setExpandRatio(panel, 1);
        
        activitiesLayout.setWidth(100, Unit.PERCENTAGE);
        activitiesLayout.setHeightUndefined();
        
        panel.setContent(activitiesLayout);
        
        updateActivities(-1);
        setComponentLeft(wrapper);
        initializeComponent();
    }
    
    private void updateActivities(long activityId) {
        activities.clear();
        activitiesLayout.removeAllComponents();
                
        try {
            List<RemoteActivityDefinition> lstActivities = wsBean.getProcessInstanceActivitiesPath(
                processInstance.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
            
            for (RemoteActivityDefinition activity : lstActivities)
                renderActivityButton(activitiesLayout, activity);
            
            boolean isFork = false;
            boolean even = false;
            paths.clear();
                        
            for (int i = 0; i < lstActivities.size(); i += 1) {
                RemoteActivityDefinition activity = lstActivities.get(i);
                
                if (activity instanceof RemoteParallelActivityDefinition) {
                    RemoteParallelActivityDefinition parallelActivityDef = (RemoteParallelActivityDefinition) activity;
                                        
                    if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.FORK) {
                        isFork = true;
                        if (parallelActivityDef.getPaths() != null) {
                            
                            for (RemoteActivityDefinition path : parallelActivityDef.getPaths())
                                paths.add(path);
                        }
                        
                    } else if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.JOIN) {
                        isFork = false;
                    }

                }
                Button btnActivity = activities.get(activity);
                
                if (isFork) {
                    
                    if (paths.contains(activity)) {
                        even = !even;                                                                                                                                                                                                                                
                    }

                    if (even) {
                        UI.getCurrent().getPage().getStyles().add(""+
                            ".nuqui .processmanager .v-button-activity-" + activity.getId() + " { "+
                            " background-color: #eceff1; " +
                            "}");
                    }
                    else {
                        UI.getCurrent().getPage().getStyles().add(""+
                            ".nuqui .processmanager .v-button-activity-" + activity.getId() + " { "+
                            " background-color: #cfd8dc; " +
                            "}");                            
                    }
                    if (paths.contains(activity))
                        btnActivity.setIcon(VaadinIcons.STAR_O);                        
                    else {
                        if (i - 1 >= 0 && activityComplete(lstActivities.get(i - 1).getId()))
                            btnActivity.setIcon(VaadinIcons.STAR_O);
                        else
                            btnActivity.setIcon(VaadinIcons.BAN);
                    }                        
                    btnActivity.addStyleName("activity-" + activity.getId());
                }
                else {
                    UI.getCurrent().getPage().getStyles().add(""+
                        ".nuqui .processmanager .v-button-activity-" + activity.getId() + " { "+
                        " background-color: #eceff1; " +
                        "}");                            
                    btnActivity.addStyleName("activity-" + activity.getId());
                    
                    btnActivity.setIcon(VaadinIcons.STAR_O);
                }
                if (activityComplete(activity.getId())) {
                    
                    if (activity.isIdling() && !idleActivityComplete(activity.getId()))
                        btnActivity.setIcon(VaadinIcons.STAR_HALF_LEFT_O);                        
                    else {
                        if (activity instanceof RemoteConditionalActivityDefinition) {
                            if (getConditionalValue(activity.getId()))
                                btnActivity.setIcon(VaadinIcons.THUMBS_UP_O); 
                            else
                                btnActivity.setIcon(VaadinIcons.THUMBS_DOWN_O); 
                        } else
                            btnActivity.setIcon(VaadinIcons.STAR);
                    }
                }
                
                if (activity instanceof RemoteParallelActivityDefinition) {
                
                    UI.getCurrent().getPage().getStyles().add(""+
                        ".nuqui .processmanager .v-button-activity-" + activity.getId() + " { "+
                        " background-color: #b0bec5; " +
                        "}");
                    btnActivity.addStyleName("activity-" + activity.getId());
                                        
                    btnActivity.setIcon(VaadinIcons.SPLIT);                                        
                }
                
                if (activity.getId() == activityId) {
                    buttonClicked = btnActivity;
                    buttonClickedResource = btnActivity.getIcon();
                    btnActivity.setIcon(VaadinIcons.CURSOR_O);
                }
            }
                        
            if (lstActivities != null && !lstActivities.isEmpty() && activityId == -1) { 
                
                RemoteActivityDefinition activityDef = lstActivities.get(lstActivities.size() - 1);
                
                if (activities.containsKey(activityDef)) {
                    
                    if (activityDef instanceof RemoteParallelActivityDefinition) {
                        RemoteParallelActivityDefinition join = (RemoteParallelActivityDefinition) activityDef;
                        if (join.getSequenceFlow() == ParallelActivityDefinition.JOIN) {
                            int forkIndex = -1;
                            int joinIndex = -1;
                            
                            for (int i = 0; i < lstActivities.size(); i++) {
                                RemoteActivityDefinition activity = lstActivities.get(i);
                                if (activity.getId() == join.getOutgoingSequenceFlowId())
                                    forkIndex = i;
                                else if (activity.getId() == join.getId())
                                    joinIndex = i;
                                if (forkIndex != -1 && joinIndex != -1)
                                    break;
                            }
                            
                            if (forkIndex != -1 && 
                                joinIndex != -1 && 
                                forkIndex + 1 < joinIndex) {
                                for (int i = forkIndex + 1; i < joinIndex; i++) {
                                    RemoteActivityDefinition activity = lstActivities.get(i);       
                                    
                                    if (actorEnabled(activity.getActor())) {
                                        try {
                                            wsBean.getArtifactForActivity(
                                                processInstance.getId(),
                                                activity.getId(),
                                                Page.getCurrent().getWebBrowser().getAddress(),
                                                remoteSession.getSessionId());
                                        } catch (ServerSideException ex) {
                                            //The artifact to render is which doesn't has artifact
                                            if (activities.containsKey(activity)) {
                                                Button btn = activities.get(activity);
                                                activities.get(activity).setIcon(VaadinIcons.FLAG_O);

                                                buttonClicked = btn;
                                                buttonClickedResource = btn.getIcon();
                                                btn.setIcon(VaadinIcons.CURSOR_O);
                                                renderArtifact(activity);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Button btn = activities.get(activityDef);
                    activities.get(activityDef).setIcon(VaadinIcons.FLAG_O);
                                        
                    buttonClicked = btn;
                    buttonClickedResource = btn.getIcon();
                    btn.setIcon(VaadinIcons.CURSOR_O);
                    
                    renderArtifact(activityDef);
                }
            }
            
        } catch (ServerSideException ex) {
            
            Notifications.showError(ex.getMessage());
        }
    }
    
    public boolean idleActivityComplete(long activityId) {
        try {
            RemoteArtifact remoteArtifact = wsBean.getArtifactForActivity(
                processInstance.getId(),
                activityId,
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
            
            if (remoteArtifact != null) {
                for (StringPair pair : remoteArtifact.getSharedInformation()) {
                    
                    if (pair.getKey().equals("__idle__"))
                        return !Boolean.valueOf(pair.getValue());
                }
            }
        } catch (ServerSideException ex) {
        }
        return false;
    }
    
    private boolean getConditionalValue(long activityId) {
        try {
            RemoteArtifact remoteArtifact = wsBean.getArtifactForActivity(
                processInstance.getId(),
                activityId,
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
            
            if (remoteArtifact != null) {
                try {
                    byte[] content = remoteArtifact.getContent();

                    XMLInputFactory xif = XMLInputFactory.newInstance();
                    ByteArrayInputStream bais = new ByteArrayInputStream(content);
                    XMLStreamReader reader = xif.createXMLStreamReader(bais);

                    QName tagValue = new QName("value"); //NOI18N

                    while (reader.hasNext()) {

                        int event = reader.next();

                        if (event == XMLStreamConstants.START_ELEMENT) {

                            if (reader.getName().equals(tagValue))
                                return Boolean.valueOf(reader.getElementText());
                        }
                    }

                } catch (Exception ex) {
                }
            }
        } catch (ServerSideException ex) {
        }
        return false;
    }
    
    public boolean activityComplete(long activityId) {
        try {
            RemoteArtifact remoteArtifact = wsBean.getArtifactForActivity(
                processInstance.getId(),
                activityId,
                Page.getCurrent().getWebBrowser().getAddress(),
                remoteSession.getSessionId());
            return remoteArtifact != null;
            
        } catch (ServerSideException ex) {
            return false;
        }
    }
    
    private boolean enableActivity(long activityId) {
        try {
            List<RemoteActivityDefinition> lstActivities = wsBean.getProcessInstanceActivitiesPath(
                processInstance.getId(), 
                Page.getCurrent().getWebBrowser().getAddress(), 
                remoteSession.getSessionId());
            
            if (lstActivities != null && !lstActivities.isEmpty()) {
                
                int activityIndex = -1;
                
                for (int i = 0; i < lstActivities.size(); i += 1) {
                    
                    if (lstActivities.get(i).getId() == activityId)
                        activityIndex = i;
                }
                if (activityIndex - 1 >= 0) {
                    //TODO: The parallel activity JOIN_FORK is not supported yet
                    for (RemoteActivityDefinition activityDef : lstActivities) {
                        
                        if (activityDef instanceof RemoteParallelActivityDefinition) {
                                                        
                            RemoteParallelActivityDefinition parallelAcvitityDef = (RemoteParallelActivityDefinition) activityDef;
                                                        
                            if (parallelAcvitityDef.getSequenceFlow() == ParallelActivityDefinition.FORK && 
                                parallelAcvitityDef.getPaths() != null) {
                                
                                for (RemoteActivityDefinition anActivityDef : parallelAcvitityDef.getPaths()) {
                                    if (anActivityDef.getId() == activityId)
                                        return true;
                                }
                            }
                        }
                    }
                    if (lstActivities.get(activityIndex) instanceof RemoteParallelActivityDefinition) {
                        RemoteParallelActivityDefinition parallelActivityDefinition = (RemoteParallelActivityDefinition) lstActivities.get(activityIndex);
                        if (parallelActivityDefinition.getSequenceFlow() == ParallelActivityDefinition.JOIN) {
                            RemoteParallelActivityDefinition join = parallelActivityDefinition;
                            try {                    
                                List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                                    processInstance.getId(), 
                                    Page.getCurrent().getWebBrowser().getAddress(), 
                                    remoteSession.getSessionId());
                                List<RemoteActivityDefinition> incomingActivityDefs = new ArrayList();
                                for (RemoteActivityDefinition item : path) {
                                    /*
                                    Get incoming RemoteConditionalActivityDefinition &  
                                    RemoteParallelActivityDefinition activities to join
                                    parallel flow are not supported yet, because
                                    in the current process definitions no are
                                    presented cases that use it, in the case of be
                                    needed this method must be recursive.
                                    */
                                    if (item.getNextActivity() != null &&
                                               item.getNextActivity().getId() == join.getId()){
                                        incomingActivityDefs.add(item);
                                    }
                                }
                                for (RemoteActivityDefinition incomingActivityDef : incomingActivityDefs) {
                                    try {
                                        wsBean.getArtifactForActivity(
                                            processInstance.getId(),
                                            incomingActivityDef.getId(),
                                            Page.getCurrent().getWebBrowser().getAddress(),
                                            remoteSession.getSessionId());  
                                    } catch (ServerSideException ex) {
                                        //Expected exception when the artifact is not found
                                        Notifications.showWarning(
                                            (join.getName() != null ? join.getName() : "Activity")
                                            + " are disable"
                                            + " meanwhile all the parallel paths of the"
                                            + " current process are not finish");
                                        return false;
                                    }
                                }
                                return true;
                            } catch(ServerSideException ex) {
                                Notifications.showError(ex.getMessage());
                            }
                        }
                    }
                    RemoteActivityDefinition activityDef = lstActivities.get(activityIndex - 1);
                                        
                    if (!(activityDef instanceof RemoteParallelActivityDefinition)) {
                        
                        wsBean.getArtifactForActivity(
                            processInstance.getId(), 
                            activityDef.getId(), 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            remoteSession.getSessionId());
                    }
                }
                return true;
            }
            
        } catch (ServerSideException ex) {
        }
        return false;
    }
}