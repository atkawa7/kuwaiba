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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessInstanceToolsView extends TabSheet {
    private final RemoteProcessDefinition remoteProcessDefinition;
    private final RemoteProcessInstance remoteProcessInstance;
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;
    private final String TIMELINE = "Timeline";
    private final String ACTIVITIES = "Activities";
    private final String PROCESS_FLOWCHART = "Process Flowchart";
        
    public ProcessInstanceToolsView(
        final RemoteProcessDefinition remoteProcessDefinition, 
        final RemoteProcessInstance remoteProcessInstance, 
        final WebserviceBean webserviceBean,
        final RemoteSession remoteSession) {
        
        this.remoteProcessDefinition = remoteProcessDefinition;
        this.remoteProcessInstance = remoteProcessInstance;
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
                
        initView();
    }    
    
    private void initView() {
        
        setSizeFull();
        
        addTab(new VerticalLayout(), ACTIVITIES, VaadinIcons.TASKS);
        addTab(new VerticalLayout(), TIMELINE, VaadinIcons.CALENDAR_CLOCK);
        addTab(new VerticalLayout(), PROCESS_FLOWCHART, VaadinIcons.SITEMAP);
        
        setSelectedTab(0);
        
        addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                setSelectedTabContent();
            }
        });
    }
    
    @Override
    public void setSelectedTab(int index) {
        super.setSelectedTab(index);
        setSelectedTabContent();
    }
    
    private void setSelectedTabContent() {
        Component tab = (Component) getSelectedTab();
        String caption = getTab(tab).getCaption();

        if (PROCESS_FLOWCHART.equals(caption)) {

            ProcessFlowchart processGraph = new ProcessFlowchart(
                remoteProcessInstance, 
                remoteProcessDefinition, 
                webserviceBean, 
                remoteSession);

            replaceComponent(tab, processGraph);
        }
        else if (ACTIVITIES.equals(caption)) {

            if (!(tab instanceof ProcessInstanceView)) {

                ProcessInstanceView processInstanceView = new ProcessInstanceView(
                    remoteProcessInstance, 
                    remoteProcessDefinition, 
                    webserviceBean, 
                    remoteSession);

                replaceComponent(tab, processInstanceView);
            }
        }
        else if (TIMELINE.equals(caption)) {
            TimelineView processTimelineView = new TimelineView(
                remoteProcessInstance, webserviceBean, remoteSession);

            replaceComponent(tab, processTimelineView);
        }
    }
}
