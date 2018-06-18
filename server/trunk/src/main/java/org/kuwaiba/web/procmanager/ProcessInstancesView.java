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

import org.kuwaiba.apis.web.gui.notifications.MessageBox;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Shows a set of process instances
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessInstancesView extends VerticalLayout {
    private final RemoteProcessDefinition processDefinition;
    private final List<RemoteProcessInstance> processes;
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
        initView();
    }
    
    public void initView() {
        VerticalLayout wrapper = new VerticalLayout();        
        wrapper.setWidth("100%");
        wrapper.setStyleName("addpadding");
        
        HorizontalLayout tools = new HorizontalLayout();
        tools.setWidth("100%");
                
        btnCreateProcessInstance = new Button("Nueva Alta de Servicio", VaadinIcons.PLUS);
                        
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
                                
                                ((ProcessManagerView) getUI().getContent()).setSecondComponent(new ProcessInstanceView(processInstance, processDefinition, wsBean,session));
                                                                                                
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
        
        grid.setItems(beans);
        grid.addColumn(ProcessInstanceBean::getOrderNumber).setCaption("Order Number");
        grid.addColumn(ProcessInstanceBean::getServiceCode).setCaption("Service Code");
        grid.addColumn(ProcessInstanceBean::getCurrentActivity).setCaption("Current Activity");
        grid.addColumn(ProcessInstanceBean::getCurrentActivityActor).setCaption("Actor");
        /*
        grid.addColumn(ProcessInstanceBean::getViewButtonCaption, new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                Notification.show("Hola");
            }
        })).setCaption("View");
        */
        ButtonRenderer buttonContinuar = new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) event.getItem();
                ((ProcessManagerView) getUI().getContent()).setSecondComponent(
                new ProcessInstanceView(processInstanceBean.getProcessInstance(), processInstanceBean.getProcessDefinition(), wsBean, session)
                );
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
                
        grid.addColumn(ProcessInstanceBean::getEditButtonCaption, buttonContinuar).setCaption("Continuar");
        grid.addColumn(ProcessInstanceBean::getViewButtonCaption, buttonView).setCaption("View");
        /*
        grid.addColumn(ProcessInstanceBean::getDeleteButtonCaption, new ButtonRenderer(new RendererClickListener<RemoteProcessInstance>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent event) {
                Notification.show("Hola");
            }
        })).setCaption("Delete");
        */
        Label lbl = new Label("Altas de Servicio");
                
        wrapper.addComponent(lbl);
        wrapper.setComponentAlignment(lbl, Alignment.TOP_CENTER);
                                
        tools.addComponent(btnCreateProcessInstance);
        
        tools.setComponentAlignment(btnCreateProcessInstance, Alignment.MIDDLE_RIGHT);
        wrapper.addComponent(tools);
        wrapper.addComponent(grid);
        
        addComponent(wrapper);
    }
}
