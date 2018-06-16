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

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalSplitPanel;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.LoginView;
import org.kuwaiba.web.modules.servmanager.ServiceManagerView;
/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("application")
public class ProcessManagerView extends VerticalSplitPanel implements View {
    public static String VIEW_NAME = "application";
    
    @Inject
    private WebserviceBeanLocal wsBean;
        
    public ProcessManagerView() {
        setStyleName("processmanager");
        addStyleName("darklayout");
        
        setSplitPosition(6, Unit.PERCENTAGE);
        setSizeFull();
    }
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyleName("mybarmenu");
        menuBar.setWidth("100%");
        
        MenuBar.MenuItem menuItem = menuBar.addItem("Processes", null, null);
        
//        try {
//            List<RemoteProcessDefinition> processDefinitions = wsBean.getProcessDefinitions(
//                Page.getCurrent().getWebBrowser().getAddress(), 
//                ((RemoteSession) getSession().getAttribute("session")).getSessionId());
//            
//            for (RemoteProcessDefinition processDefinition : processDefinitions) {
//                
//                menuItem.addItem(processDefinition.getName(), null, new MenuBar.Command() {
//                    @Override
//                    public void menuSelected(MenuBar.MenuItem selectedItem) {
//                        
//                        try {
//                            List<RemoteProcessInstance> processInstances = wsBean.getProcessInstances(
//                                processDefinition.getId(), 
//                                Page.getCurrent().getWebBrowser().getAddress(), 
//                                ((RemoteSession) getSession().getAttribute("session")).getSessionId());
//                            
//                            setSecondComponent(new ProcessInstancesView(processDefinition, processInstances, wsBean, ((RemoteSession) getSession().getAttribute("session"))));
//                                                        
//                        } catch (ServerSideException ex) {
//                            NotificationsUtil.showError(ex.getMessage());
//                        }
//                    }
//                });
//            }
//            
//        } catch (ServerSideException ex) {
//            NotificationsUtil.showError(ex.getMessage());
//        }
        
        menuBar.addItem("Service Manager", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(ServiceManagerView.VIEW_NAME);
            }
        });
        
        menuBar.addItem("Log out", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                RemoteSession session = (RemoteSession) getSession().getAttribute("session");
                try {
                    wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                    getSession().setAttribute("session", null);
                    getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
                    
                } catch (ServerSideException ex) {
                    NotificationsUtil.showError(ex.getMessage());
                }
            }
        });
                
        setFirstComponent(menuBar);
    }
    
}
