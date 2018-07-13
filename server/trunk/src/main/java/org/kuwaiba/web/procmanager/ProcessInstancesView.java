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
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConditionalActivityDefinition;
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
        VerticalLayout wrapper = new VerticalLayout();        
        wrapper.setWidth("100%");
        wrapper.setStyleName("addpadding");
        
        HorizontalLayout tools = new HorizontalLayout();
        tools.setWidth("100%");
                
        btnCreateProcessInstance = new Button("New Service", VaadinIcons.PLUS);
                        
        btnCreateProcessInstance.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                MessageBox.getInstance().showMessage(new Label("Create an instance of the process")).addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (MessageBox.getInstance().continues()) {
                            try {
                                long id = wsBean.createProcessInstance
                                        (processDefinition.getId(),
                                                "",
                                                "",
                                                Page.getCurrent().getWebBrowser().getAddress(),
                                                ((RemoteSession) getSession().getAttribute("session")).getSessionId());
                                
                                RemoteProcessInstance processInstance = wsBean.getProcessInstance(
                                        id, 
                                        Page.getCurrent().getWebBrowser().getAddress(),
                                        ((RemoteSession) getSession().getAttribute("session")).getSessionId());
                                
                                UI ui = getUI();
                                
                                MenuBar mainMenu = ((IndexUI) ui).getMainMenu();
                                
                                ((ProcessManagerComponent) ui.getContent()).removeAllComponents();
                                
                                ((ProcessManagerComponent) ui.getContent()).addComponent(mainMenu);
                                ((ProcessManagerComponent) ui.getContent()).setExpandRatio(mainMenu, 0.5f);
                                
                                ProcessInstanceView processInstanceView = new ProcessInstanceView(processInstance, processDefinition, wsBean,session);
                                
                                ((ProcessManagerComponent) ui.getContent()).addComponent(processInstanceView);
                                ((ProcessManagerComponent) ui.getContent()).setExpandRatio(processInstanceView, 9.5f);
                                                                                                
                            } catch (ServerSideException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            
                        }
                    }
                });
                    
            }
        });
                
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setWidth("100%");
        
        List<ProcessInstanceBean> beans = new ArrayList();
                
        for (RemoteProcessInstance process : processes)
            beans.add(new ProcessInstanceBean(process, wsBean, session));
        
        String columnOrderNumberId = "columnOrderNumber"; //NOI18N
        String columnServiceCodeId = "columnServiceCode"; //NOI18N
        String columnCurrentActivityId = "columnCurrentActivity"; //NOI18N
        String columnActorId = "columnActor"; //NOI18N
        String columnStatusId = "columnStatus"; //NOI18N        
        
        grid.setItems(beans);
        grid.addColumn(ProcessInstanceBean::getOrderNumber).setCaption("Order Number").setId(columnOrderNumberId);
        grid.addColumn(ProcessInstanceBean::getServiceCode).setCaption("Service Code").setId(columnServiceCodeId);
        grid.addColumn(ProcessInstanceBean::getCurrentActivity).setCaption("Current Activity").setId(columnCurrentActivityId);
        grid.addColumn(ProcessInstanceBean::getCurrentActivityActor).setCaption("Actor").setId(columnActorId);
        
        HeaderRow headerRow = grid.appendHeaderRow();
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
                
        grid.addColumn(ProcessInstanceBean::getEditButtonCaption, buttonContinuar).setCaption("Status").setId("columnStatus");
        grid.addColumn(ProcessInstanceBean::getViewButtonCaption, buttonView).setCaption("View");        
        grid.addColumn(ProcessInstanceBean::getDeleteButtonCaption, new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                
                MessageBox.getInstance().showMessage(new Label("Create an instance of the process")).addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                                                
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
        // Filter To Status
        HeaderCell statusHeaderCell = headerRow.getCell(columnStatusId);
        
        ComboBox cmbStatus = new ComboBox();
        
        List<String> status = new ArrayList();
        status.add("Created");
        status.add("Finalized");
        status.add("Continue");
        
        cmbStatus.setItems(status);
        statusHeaderCell.setComponent(cmbStatus);
        
        cmbStatus.addValueChangeListener(new ValueChangeListener<String>() {
            
            @Override
            public void valueChange(HasValue.ValueChangeEvent<String> event) {
                Iterator<ProcessInstanceBean> iterator = beans.iterator();
                
                List<ProcessInstanceBean> filteredItems = new ArrayList();
                
                while (iterator.hasNext()) {
                    ProcessInstanceBean element = iterator.next();

                    if (captionFilter.test(element != null ? element.getEditButtonCaption() : null, event.getValue() != null ? event.getValue() : ""))
                        filteredItems.add(element);
                }
                grid.setItems(filteredItems);
            }
        });
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
                
        Label lbl = new Label("Services");
                
        wrapper.addComponent(lbl);
        wrapper.setComponentAlignment(lbl, Alignment.TOP_CENTER);
                                
        tools.addComponent(btnCreateProcessInstance);
        
        tools.setComponentAlignment(btnCreateProcessInstance, Alignment.MIDDLE_RIGHT);
        wrapper.addComponent(tools);
        wrapper.addComponent(grid);
        
        addComponent(wrapper);
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
