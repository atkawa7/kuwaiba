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
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessInstancesView extends VerticalLayout {
    private RemoteProcessDefinition processDefinition;
    private List<RemoteProcessInstance> processes;
    private Grid<RemoteProcessInstance> grid;
        
    private TextField txtFilter;
    private Button btnCreateProcessInstance;
    
    private final WebserviceBeanLocal wsBean;
            
    public ProcessInstancesView(RemoteProcessDefinition processDefinition, List<RemoteProcessInstance> processes, WebserviceBeanLocal wsBean) {
        setStyleName("darklayout");
        setSizeFull();
        this.processDefinition = processDefinition;
        this.processes = processes;
        this.wsBean = wsBean;
        initView();
    }
    
    public void initView() {
        VerticalLayout wrapper = new VerticalLayout();        
        wrapper.setWidth("100%");
        wrapper.setStyleName("addpadding");
        
        HorizontalLayout tools = new HorizontalLayout();
        tools.setWidth("100%");
        
        txtFilter = new TextField();
        
        btnCreateProcessInstance = new Button("Crear Alta de un servicio");
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
                                
                                ((MainView) getUI().getContent()).setSecondComponent(new NewProcessInstanceView(processInstance, processDefinition, wsBean));
                                                                                                
                            } catch (ServerSideException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            
                        }
                    }
                });
                    
            }
        });
        grid = new Grid<>();
        grid.setWidth("100%");
        
        grid.setItems(processes);
        grid.addColumn(RemoteProcessInstance::getId).setCaption("Process Instance Id");
        grid.addColumn(RemoteProcessInstance::getName).setCaption("Order number");
        
        tools.addComponent(txtFilter);        
        tools.addComponent(btnCreateProcessInstance);
        tools.setComponentAlignment(txtFilter, Alignment.MIDDLE_LEFT);
        tools.setComponentAlignment(btnCreateProcessInstance, Alignment.MIDDLE_RIGHT);
        wrapper.addComponent(tools);
        wrapper.addComponent(grid);
        
        addComponent(wrapper);
    }
}


