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
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConditionalActivityDefinition;
import org.kuwaiba.util.i18n.I18N;
import org.kuwaiba.web.IndexUI;

/**
 * Shows a set of process instances
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessInstancesView extends VerticalLayout {
    private final RemoteProcessDefinition processDefinition;
    private List<RemoteProcessInstance> processes;
    private Grid<ProcessInstanceBean> grid;
    
    private Button btnCreateProcessInstance;
    
    private final WebserviceBean wsBean;
    private final RemoteSession session;
            
    public ProcessInstancesView(RemoteProcessDefinition processDefinition, List<RemoteProcessInstance> processes, WebserviceBean wsBean, RemoteSession session) {
        setStyleName("darklayout");
        setSizeFull();
        this.processDefinition = processDefinition;
        this.processes = processes;
        this.wsBean = wsBean;
        this.session = session;
        
        getAllActivities(processDefinition.getStartActivity());
        
        initView();
    }
    
    public void initView() {
//        setSizeFull();
//        setSpacing(false);
        
//        VerticalLayout wrapper = new VerticalLayout();        
////        wrapper.setWidth("100%");
//        wrapper.setStyleName("addpadding");
        
        HorizontalLayout tools = new HorizontalLayout();
        tools.setWidth("100%");
                
        btnCreateProcessInstance = new Button(I18N.gm("new"), VaadinIcons.PLUS);
                        
        btnCreateProcessInstance.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                
                createProcessInstance(processDefinition, wsBean, session);
            }
        });
                
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setWidth("100%");
        
        UI.getCurrent().getPage().getStyles().add(".v-grid tr th, .v-grid tr td { height: 30px; }");
        
        List<ProcessInstanceBean> beans = new ArrayList();
                
        for (RemoteProcessInstance process : processes)
            beans.add(new ProcessInstanceBean(process, wsBean, session));
        
        String columnOrderNumberId = "columnOrderNumber"; //NOI18N
        String columnServiceCodeId = "columnServiceCode"; //NOI18N
        String columnCurrentActivityId = "columnCurrentActivity"; //NOI18N
        String columnActorId = "columnActor"; //NOI18N
        String columnStatusId = "columnStatus"; //NOI18N        
        String columnViewId = "columnView"; //NOI18N
        
        grid.setItems(beans);
        
        final String NEW_SERVICE = "New Service"; //NOI18N
        HeaderRow headerRow = grid.appendHeaderRow();
        
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
        /*                
        ButtonRenderer buttonContinuar = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                
                UI ui = getUI();
                
                MenuBar mainMenu = ((IndexUI) ui).getMainMenu();
                
                ((ProcessManagerComponent) ui.getContent()).removeAllComponents();
                
                ((ProcessManagerComponent) ui.getContent()).addComponent(mainMenu);
                ((ProcessManagerComponent) ui.getContent()).setExpandRatio(mainMenu, 0.5f);

                ProcessInstanceView processInstanceView = new ProcessInstanceView(processInstanceBean.getProcessInstance(), processInstanceBean.getProcessDefinition(), wsBean, session);

                ((ProcessManagerComponent) ui.getContent()).addComponent(processInstanceView);
                ((ProcessManagerComponent) ui.getContent()).setExpandRatio(processInstanceView, 9.5f);
            }
        });        
        ButtonRenderer buttonView = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                
                ProcessGraph processGraph = new ProcessGraph(
                    processInstanceBean.getProcessInstance(), 
                    processInstanceBean.getProcessDefinition(), 
                    wsBean, 
                    session);
                Window newWindow = new Window();
                newWindow.setWidth(80, Unit.PERCENTAGE);
                newWindow.setHeight(80, Unit.PERCENTAGE);
                newWindow.setModal(true);
                newWindow.setContent(processGraph);
                getUI().addWindow(newWindow);
            }
        });
        ButtonRenderer btnTimeline = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                
//                    ProcessInstanceIndicatorView view = new ProcessInstanceIndicatorView(
//                        processInstanceBean.getProcessInstance(), 
//                        wsBean, 
//                        session);
                    TimelineView view = new TimelineView(
                        wsBean,
                        processInstanceBean.getProcessDefinition(),
                        processInstanceBean.getProcessInstance(), 
                        session);
                    
                    Window window = new Window();       
                    window.setWidth(100, Unit.PERCENTAGE);
                    window.setHeight(100, Unit.PERCENTAGE);
                    window.setModal(true);
                    window.setContent(view);
                    getUI().addWindow(window);
            }
        });
                        
        grid.addColumn(ProcessInstanceBean::getEditButtonCaption, buttonContinuar).setCaption("Status").setId(columnStatusId);
        grid.addColumn(ProcessInstanceBean::getTimelineButtonCaption, btnTimeline).setCaption("Timeline");
        grid.addColumn(ProcessInstanceBean::getViewButtonCaption, buttonView).setCaption("View").setId(columnViewId);  
        */
        grid.addSelectionListener(new SelectionListener<ProcessInstanceBean>() {
            @Override
            public void selectionChange(SelectionEvent<ProcessInstanceBean> event) {
                Optional<ProcessInstanceBean> optional = event.getFirstSelectedItem();
                ProcessInstanceBean processInstanceBean = optional.get();
                
                UI ui = getUI();
                
                MenuBar mainMenu = ((IndexUI) ui).getMainMenu();
                
                ((ProcessManagerComponent) ui.getContent()).removeAllComponents();
                
                ((ProcessManagerComponent) ui.getContent()).addComponent(mainMenu);
                ((ProcessManagerComponent) ui.getContent()).setExpandRatio(mainMenu, 0.5f);

                TimelineView timelineView = new TimelineView(
                    wsBean,
                    processInstanceBean.getProcessDefinition(),
                    processInstanceBean.getProcessInstance(), 
                    session);

                ((ProcessManagerComponent) ui.getContent()).addComponent(timelineView);
                ((ProcessManagerComponent) ui.getContent()).setExpandRatio(timelineView, 9.5f);
            }
        });
                
        if (canDelete()) {
            
            grid.addColumn(ProcessInstanceBean::getDeleteButtonCaption, new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
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
            })).setCaption("Delete");    
        }
        // Filter To Status
////        HeaderCell statusHeaderCell = headerRow.getCell(columnStatusId);
////        
////        ComboBox cmbStatus = new ComboBox();
////        
////        List<String> status = new ArrayList();
////        status.add("Created");
////        status.add("Finalized");
////        status.add("Continue");
////        
////        cmbStatus.setItems(status);
////        statusHeaderCell.setComponent(cmbStatus);
////        
////        cmbStatus.addValueChangeListener(new ValueChangeListener<String>() {
////            
////            @Override
////            public void valueChange(HasValue.ValueChangeEvent<String> event) {
////                Iterator<ProcessInstanceBean> iterator = beans.iterator();
////                
////                List<ProcessInstanceBean> filteredItems = new ArrayList();
////                
////                while (iterator.hasNext()) {
////                    ProcessInstanceBean element = iterator.next();
////
////                    if (captionFilter.test(element != null ? element.getEditButtonCaption() : null, event.getValue() != null ? event.getValue() : ""))
////                        filteredItems.add(element);
////                }
////                grid.setItems(filteredItems);
////            }
////        });
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
                
        Label lblProcessDefinitionName = new Label((processDefinition.getName() != null ? processDefinition.getName() : "") + " Processes");
        lblProcessDefinitionName.addStyleNames(ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD);
                                
        tools.addComponent(btnCreateProcessInstance);
        tools.setComponentAlignment(btnCreateProcessInstance, Alignment.MIDDLE_RIGHT);
        
        addComponent(lblProcessDefinitionName);
        addComponent(tools);
        addComponent(grid);
        
        grid.setHeight(90, Unit.PERCENTAGE);
        
        setComponentAlignment(lblProcessDefinitionName, Alignment.TOP_CENTER);
        setExpandRatio(lblProcessDefinitionName, 0.05f);
        setExpandRatio(tools, 0.05f);
        setExpandRatio(grid, 0.8f);
        
//        wrapper.setSizeFull();
                
        setSpacing(false);
//        addComponent(wrapper);
////        setComponentAlignment(wrapper, Alignment.TOP_CENTER);
//        setExpandRatio(wrapper, 1f);
    }
    
    private boolean canDelete() {
        try {
            List<GroupInfoLight> groups = wsBean.getGroupsForUser(
                session.getUserId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                session.getSessionId());
            
            for (GroupInfoLight group : groups) {

                if ("Commercial".equals(group.getName())) //NOI18N
                    return true;
            }
            
            return false;
            
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
                        
            return false;
        }
    }
    
    public static void createProcessInstance(RemoteProcessDefinition processDef, WebserviceBean webserviceBean, RemoteSession remoteSession) {
                
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

                        UI ui = UI.getCurrent();

                        MenuBar mainMenu = ((IndexUI) ui).getMainMenu();

                        ((ProcessManagerComponent) ui.getContent()).removeAllComponents();

                        ((ProcessManagerComponent) ui.getContent()).addComponent(mainMenu);
                        ((ProcessManagerComponent) ui.getContent()).setExpandRatio(mainMenu, 0.5f);

                        ProcessInstanceView processInstanceView = new ProcessInstanceView(processInstance, processDef, webserviceBean,remoteSession);

                        ((ProcessManagerComponent) ui.getContent()).addComponent(processInstanceView);
                        ((ProcessManagerComponent) ui.getContent()).setExpandRatio(processInstanceView, 9.5f);
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
            } else {
                getAllActivities(activity.getNextActivity());
            }
        }
    }
}
