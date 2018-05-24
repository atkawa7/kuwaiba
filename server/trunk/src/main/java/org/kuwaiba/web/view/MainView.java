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

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalSplitPanel;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBeanLocal;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("application")
public class MainView extends VerticalSplitPanel implements View {
    public static String VIEW_NAME = "application";
    
    @Inject
    private WebserviceBeanLocal wsBean;
        
    public MainView() {
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
        
        MenuBar.MenuItem menuItem = menuBar.addItem("Process", null, null);
        menuItem.addItem("Alta de un servicio", null, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                setSecondComponent(new ProcessInstancesView(UtilProcess.getProcessInstances(UtilProcess.getProcessDefinition1()), wsBean));
            }
        });
        menuBar.addItem("Service Manager", null, null);
                
        setFirstComponent(menuBar);
    }
    
}
