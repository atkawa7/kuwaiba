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
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import org.kuwaiba.apis.web.gui.notifications.MessageBox;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConditionalActivityDefinition;
import org.kuwaiba.util.i18n.I18N;
import org.kuwaiba.web.IndexUI;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteParallelActivityDefinition;

/**
 * Shown the instances of a process definition
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessInstancesView extends VerticalLayout {
    private final RemoteProcessDefinition processDefinition;
    private List<RemoteProcessInstance> processes;
    private Grid<ProcessInstanceBean> grid;
    
    private Button btnCreateProcessInstance;
    
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    
    public static Boolean debugMode;
            
    public ProcessInstancesView(RemoteProcessDefinition processDefinition, List<RemoteProcessInstance> processes, WebserviceBean wsBean, RemoteSession session) {
        
        debugMode = Boolean.valueOf(String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("debugMode")));
        
        setSizeFull();
        setSpacing(false);
        setMargin(false);
                
        this.processDefinition = processDefinition;
        this.processes = processes;
        this.wsBean = wsBean;
        this.session = session;
        
        getAllActivities(processDefinition.getStartActivity());
        
        initView();
    }
    
    public static void setActionComponent(Component component, ProcessInstancesView processInstancesView) {
        UI ui = UI.getCurrent().getUI();
        
        MenuBar mainMenu = ((IndexUI) ui).getMainMenu();
        
        ((ProcessManagerComponent) ui.getContent()).removeAllComponents();
        
        ((ProcessManagerComponent) ui.getContent()).addComponent(mainMenu);
        ((ProcessManagerComponent) ui.getContent()).setExpandRatio(mainMenu, 0.3f);
                
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(false);
        verticalLayout.setMargin(false);
        
        if (processInstancesView != null) {
            Button btnBack = new Button();
            btnBack.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            btnBack.setIcon(VaadinIcons.ARROW_BACKWARD);
            btnBack.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    setActionComponent(processInstancesView, null);
                }
            });
            verticalLayout.addComponent(btnBack);
            verticalLayout.setExpandRatio(btnBack, 0.5f);
        }
        verticalLayout.addComponent(component);
        verticalLayout.setExpandRatio(component, 9.5f);
        
        ((ProcessManagerComponent) ui.getContent()).addComponent(verticalLayout);        
        ((ProcessManagerComponent) ui.getContent()).setExpandRatio(verticalLayout, 9.7f);
    }
    
    public void initView() {
        HorizontalLayout tools = new HorizontalLayout();
        tools.setSpacing(false);
        tools.setMargin(false);
        tools.setWidth(80, Unit.PERCENTAGE);
        tools.setHeight(100, Unit.PERCENTAGE);
                
        btnCreateProcessInstance = new Button(I18N.gm("new"), VaadinIcons.PLUS);
                        
        btnCreateProcessInstance.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                
                createProcessInstance(ProcessInstancesView.this, processDefinition, wsBean, session);
            }
        });
                
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setWidth(80, Unit.PERCENTAGE);
        grid.setHeight(90, Unit.PERCENTAGE);
        
        UI.getCurrent().getPage().getStyles().add(".v-grid tr th, .v-grid tr td { height: 34px; }");
        
        List<ProcessInstanceBean> beans = new ArrayList();
                
        for (RemoteProcessInstance process : processes)
            beans.add(new ProcessInstanceBean(process, wsBean, session));
        
        String columnProcessId = "columnProcessId"; //NOI18N
        String columnOrderNumberId = "columnOrderNumber"; //NOI18N
        String columnServiceCodeId = "columnServiceCode"; //NOI18N
        String columnCurrentActivityId = "columnCurrentActivity"; //NOI18N
        String columnActorId = "columnActor"; //NOI18N
        String columnStatusId = "columnStatus"; //NOI18N        
        String columnViewId = "columnView"; //NOI18N
        
        grid.setItems(beans);
        
        final String NEW_SERVICE = "New Service"; //NOI18N
        HeaderRow headerRow = grid.appendHeaderRow();
        
        grid.addColumn(ProcessInstanceBean::getProcessId).setCaption("Process Id").setId(columnProcessId).setWidthUndefined();
        
        if (processDefinition.getName().equals(NEW_SERVICE)) {
            grid.addColumn(ProcessInstanceBean::getOrderNumber).setCaption("Order Number").setId(columnOrderNumberId);
            grid.addColumn(ProcessInstanceBean::getServiceCode).setCaption("Service Code").setId(columnServiceCodeId);
            
            // Filter to Order Number                
            HeaderCell orderNumberHeaderCell = headerRow.getCell(columnOrderNumberId);

            TextField txtOrderNumber = new TextField();
            orderNumberHeaderCell.setComponent(txtOrderNumber);

            txtOrderNumber.addValueChangeListener(new ValueChangeListener<String>() {

                @Override
                public void valueChange(HasValue.ValueChangeEvent<String> event) {
                    Iterator<ProcessInstanceBean> iterator = beans.iterator();

                    List<ProcessInstanceBean> filteredItems = new ArrayList();

                    while (iterator.hasNext()) {
                        ProcessInstanceBean element = iterator.next();

                        if (captionFilter.test(element != null ? element.getOrderNumber() : null, event.getValue()))
                            filteredItems.add(element);
                    }
                    grid.setItems(filteredItems);
                }
            });
            // Filter to Service Code
            HeaderCell serviceCodeHeaderCell = headerRow.getCell(columnServiceCodeId);

            TextField txtServiceCode = new TextField();
            serviceCodeHeaderCell.setComponent(txtServiceCode);

            txtServiceCode.addValueChangeListener(new ValueChangeListener<String>() {

                @Override
                public void valueChange(HasValue.ValueChangeEvent<String> event) {
                    Iterator<ProcessInstanceBean> iterator = beans.iterator();

                    List<ProcessInstanceBean> filteredItems = new ArrayList();

                    while (iterator.hasNext()) {
                        ProcessInstanceBean element = iterator.next();

                        if (captionFilter.test(element != null ? element.getServiceCode() : null, event.getValue()))
                            filteredItems.add(element);
                    }
                    grid.setItems(filteredItems);
                }
            });
        }
        grid.addColumn(ProcessInstanceBean::getCurrentActivity, new HtmlRenderer()).setCaption("Current Activity").setId(columnCurrentActivityId);
        grid.addColumn(ProcessInstanceBean::getCurrentActivityActor).setCaption("Actor").setId(columnActorId);
                  
        ButtonRenderer buttonContinuar = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                
                ProcessInstanceView processInstanceView = new ProcessInstanceView(
                    processInstanceBean.getProcessInstance(), 
                    processInstanceBean.getProcessDefinition(), 
                    wsBean, 
                    session);
                
                setActionComponent(processInstanceView, ProcessInstancesView.this);
            }
        });
        buttonContinuar.setHtmlContentAllowed(true);
        ButtonRenderer buttonView = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                
                ProcessGraph processGraph = new ProcessGraph(
                    processInstanceBean.getProcessInstance(), 
                    processInstanceBean.getProcessDefinition(), 
                    wsBean, 
                    session);
                
                setActionComponent(processGraph, ProcessInstancesView.this);
            }
        });
        buttonView.setHtmlContentAllowed(true);
        ButtonRenderer btnTimeline = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                TimelineView timelineView = new TimelineView(
                    processInstanceBean.getProcessInstance(),
                    wsBean,
                    session);
                
                setActionComponent(timelineView, ProcessInstancesView.this);
            }
        });      
        btnTimeline.setHtmlContentAllowed(true);
        
        grid.addColumn(ProcessInstanceBean::getEditButtonCaption, buttonContinuar)
            .setId(columnStatusId)
            .setMinimumWidth(50f)
            .setMaximumWidth(50f)
            .setDescriptionGenerator(e -> "<b>Activities</b>", ContentMode.HTML);
        
        grid.addColumn(ProcessInstanceBean::getTimelineButtonCaption, btnTimeline)
            .setMinimumWidth(50f)
            .setMaximumWidth(50f)
            .setDescriptionGenerator(e -> "<b>Timeline</b>", ContentMode.HTML);
        
        grid.addColumn(ProcessInstanceBean::getViewButtonCaption, buttonView)
            .setId(columnViewId)
            .setMinimumWidth(50f)
            .setMaximumWidth(50f)
            .setDescriptionGenerator(e -> "<b>Graph</b>", ContentMode.HTML);
        
        grid.addSelectionListener(new SelectionListener<ProcessInstanceBean>() {
            @Override
            public void selectionChange(SelectionEvent<ProcessInstanceBean> event) {
                Optional<ProcessInstanceBean> optional = event.getFirstSelectedItem();
                
                if (optional.isPresent()) {
                    ProcessInstanceBean processInstanceBean = optional.get();
                    
                    ProcessInstanceToolsView processInstanceToolsView = new ProcessInstanceToolsView(
                        processInstanceBean.getProcessDefinition(),
                        processInstanceBean.getProcessInstance(), 
                        wsBean, 
                        session);

                    setActionComponent(processInstanceToolsView, ProcessInstancesView.this);
                }
            }
        });
        
        if (debugMode) {}
        ButtonRenderer deleteButtonRenderer = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();

                if ("".equals(processInstanceBean.getDeleteButtonCaption()))
                    return;

                MessageBox.getInstance().showMessage(new Label("Delete an instance of the process")).addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        event.getButton().getCaption();

                        if (MessageBox.getInstance().continues()) {
                            try {
                                String address = Page.getCurrent().getWebBrowser().getAddress();
                                String sesionId = ((RemoteSession) getSession().getAttribute("session")).getSessionId();

                                wsBean.deleteProcessInstance(
                                    processInstanceBean.getProcessInstance().getId(),
                                    address,
                                    sesionId);
                                // Updating the rows in the grid
                                processes.clear();
                                beans.clear();

                                processes = wsBean.getProcessInstances(
                                    processDefinition.getId(), 
                                    address, 
                                    sesionId);

                                for (RemoteProcessInstance process : processes)
                                    beans.add(new ProcessInstanceBean(process, wsBean, session));

                                grid.setItems(beans);

                            } catch (ServerSideException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                });
            }
        });
        deleteButtonRenderer.setHtmlContentAllowed(true);
        
        grid.addColumn(ProcessInstanceBean::getDeleteButtonCaption, deleteButtonRenderer)
            .setMinimumWidth(50f)
            .setMaximumWidth(50f)
            .setDescriptionGenerator(e -> "<b>Delete</b>", ContentMode.HTML);
        // Filter To Current Activity
        HeaderCell currentActivityHeaderCell = headerRow.getCell(columnCurrentActivityId);
        
        ComboBox cmbCurrentActivity = new ComboBox();
        
        cmbCurrentActivity.setItems(allActivities);
        currentActivityHeaderCell.setComponent(cmbCurrentActivity);
        
        cmbCurrentActivity.addValueChangeListener(new ValueChangeListener<RemoteActivityDefinition>() {
            
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteActivityDefinition> event) {
                Iterator<ProcessInstanceBean> iterator = beans.iterator();
                
                List<ProcessInstanceBean> filteredItems = new ArrayList();
                
                while (iterator.hasNext()) {
                    ProcessInstanceBean element = iterator.next();

                    if (captionFilter.test(element != null ? element.getCurrentActivity() : null, event.getValue() != null ? event.getValue().toString() : ""))
                        filteredItems.add(element);
                }
                grid.setItems(filteredItems);
            }
        });
        // Filter To Actor
        HeaderCell actorHeaderCell = headerRow.getCell(columnActorId);
        
        ComboBox cmbActor = new ComboBox();
        
        cmbActor.setItems(allActors);
        actorHeaderCell.setComponent(cmbActor);
        
        cmbActor.addValueChangeListener(new ValueChangeListener<RemoteActor>() {
            
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteActor> event) {
                Iterator<ProcessInstanceBean> iterator = beans.iterator();
                
                List<ProcessInstanceBean> filteredItems = new ArrayList();
                
                while (iterator.hasNext()) {
                    ProcessInstanceBean element = iterator.next();

                    if (captionFilter.test(element != null ? element.getCurrentActivityActor() : null, event.getValue() != null ? event.getValue().toString() : ""))
                        filteredItems.add(element);
                }
                grid.setItems(filteredItems);                
            }
        });
                
        Label lblProcessDefinitionName = new Label("<h2>" + (processDefinition.getName() != null ? processDefinition.getName() : "") + " Processes" + "</h2>", ContentMode.HTML);
                                        
        tools.addComponent(btnCreateProcessInstance);
        tools.setComponentAlignment(btnCreateProcessInstance, Alignment.MIDDLE_RIGHT);
        
        addComponent(lblProcessDefinitionName);
        addComponent(tools);
        addComponent(grid);
                
        setComponentAlignment(lblProcessDefinitionName, Alignment.MIDDLE_CENTER);
        setComponentAlignment(tools, Alignment.MIDDLE_CENTER);
        setComponentAlignment(grid, Alignment.MIDDLE_CENTER);
        
        setExpandRatio(lblProcessDefinitionName, 0.05f);
        setExpandRatio(tools, 0.05f);
        setExpandRatio(grid, 0.95f);
    }
    
////    private boolean canDelete() {
////        try {
////            List<GroupInfoLight> groups = wsBean.getGroupsForUser(
////                session.getUserId(),
////                Page.getCurrent().getWebBrowser().getAddress(),
////                session.getSessionId());
////            
////            for (GroupInfoLight group : groups) {
////
////                if ("Commercial".equals(group.getName())) //NOI18N
////                    return true;
////            }
////            
////            return false;
////            
////        } catch (ServerSideException ex) {
////            Notifications.showError(ex.getMessage());
////                        
////            return false;
////        }
////    }
        
    public static void createProcessInstance(ProcessInstancesView processInstancesView, RemoteProcessDefinition processDef, WebserviceBean webserviceBean, RemoteSession remoteSession) {
        if (processDef == null || webserviceBean == null || remoteSession == null) {
            Notifications.showError("Can not create a process instance");
            return;
        }
        RemoteActivityDefinition startActivity = processDef.getStartActivity();
        if (startActivity != null) {
            RemoteActor remoteActor = startActivity.getActor();
            if (remoteActor != null) {
                if(!actorEnabled(webserviceBean, remoteSession, remoteActor)) {
                    Notifications.showError(I18N.gm("procmanager.user_is_not_authorized_to_perform_this_action"));
                    return;
                }
            }
            else {
                Notifications.showError("The actor of the start activity cannot be found");
                return;
            }
        }
        else {
            Notifications.showError("Start activity cannot be found");
            return;
        }
        
        MessageBox.getInstance().showMessage(new Label("Create an instance of the process")).addClickListener(new Button.ClickListener() {
                                                
            @Override
            public void buttonClick(Button.ClickEvent event) {
                
                if (MessageBox.getInstance().continues()) {

                    try {
                        long id = webserviceBean.createProcessInstance
                                (processDef.getId(),
                                        "",
                                        "",
                                        Page.getCurrent().getWebBrowser().getAddress(),
                                        remoteSession.getSessionId());

                        RemoteProcessInstance processInstance = webserviceBean.getProcessInstance(
                                id, 
                                Page.getCurrent().getWebBrowser().getAddress(),
                                remoteSession.getSessionId());
                                                                        
                        ProcessInstanceView processInstanceView = new ProcessInstanceView(processInstance, processDef, webserviceBean,remoteSession);
                        setActionComponent(processInstanceView, processInstancesView);
                        
                    } catch (ServerSideException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }
        
    private final ComboBox.CaptionFilter captionFilter = new ComboBox.CaptionFilter() {        
        
        @Override
        public boolean test(String itemCaption, String filterText) {
            
            if (itemCaption == null || filterText == null)
                return false;
            
            return itemCaption.toLowerCase().contains(filterText.toLowerCase());
        }
    };
    
    private final List<RemoteActivityDefinition> allActivities = new ArrayList();
    private final List<RemoteActor> allActors = new ArrayList();
    
    private void getAllActivities(RemoteActivityDefinition activity) {
        if (activity != null && !allActivities.contains(activity)) {
            allActivities.add(activity);
            
            if (!allActors.contains(activity.getActor()))
                allActors.add(activity.getActor());
            
            if (activity instanceof RemoteConditionalActivityDefinition) {
                getAllActivities(((RemoteConditionalActivityDefinition) activity).getNextActivityIfTrue());
                getAllActivities(((RemoteConditionalActivityDefinition) activity).getNextActivityIfFalse());
            } 
            else if (activity instanceof RemoteParallelActivityDefinition && 
                    ((RemoteParallelActivityDefinition) activity).getPaths() != null) {
                for (RemoteActivityDefinition path : ((RemoteParallelActivityDefinition) activity).getPaths()) {
                    getAllActivities(path);                    
                }
            }
            else {
                getAllActivities(activity.getNextActivity());
            }
        }
    }
    
    private static boolean actorEnabled(WebserviceBean webserviceBean, RemoteSession remoteSession, RemoteActor actor) {
        try {
            List<GroupInfoLight> groups = webserviceBean.getGroupsForUser(
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
}
