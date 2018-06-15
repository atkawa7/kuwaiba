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

import com.vaadin.pontus.vizcomponent.VizComponent;
import com.vaadin.pontus.vizcomponent.model.Graph;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConditionalActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Shows graphically the process definition and the current state of a process instance
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessGraph extends Panel {
    private final RemoteProcessDefinition processDefinition;
    private final RemoteProcessInstance processInstance;
    private final WebserviceBeanLocal wsBean;
    private final RemoteSession remoteSession;
    private final HashMap<Long, RemoteActivityDefinition> ids = new HashMap();
    private final List<RemoteActivityDefinition> allActivities = new ArrayList();
    
    public ProcessGraph(RemoteProcessInstance processInstance, RemoteProcessDefinition processDefinition, WebserviceBeanLocal wsBean, RemoteSession remoteSession) {
        this.processDefinition = processDefinition;
        this.processInstance = processInstance;
        this.wsBean = wsBean;
        this.remoteSession = remoteSession;
        setSizeFull();
        render();
    }
    
    public void getAllActivities(RemoteActivityDefinition activity) {
        if (activity != null && !allActivities.contains(activity)) {
            allActivities.add(activity);
            
            if (activity instanceof RemoteConditionalActivityDefinition) {
                getAllActivities(((RemoteConditionalActivityDefinition) activity).getNextActivityIfTrue());
                getAllActivities(((RemoteConditionalActivityDefinition) activity).getNextActivityIfFalse());
            } else {
                getAllActivities(activity.getNextActivity());
            }
        }
    }
    
    public void addEges(HashMap<RemoteActivityDefinition, Graph.Node> activities, RemoteActivityDefinition activity, Graph graph) {
        if (activity != null) {
            
            if (activity instanceof RemoteConditionalActivityDefinition) {
                RemoteActivityDefinition ifTrue = ((RemoteConditionalActivityDefinition) activity).getNextActivityIfTrue();
                if (ifTrue == null)
                    return;
                
                if (graph.getEdge(activities.get(activity), activities.get(ifTrue)) != null)                
                    return;
                
                graph.addEdge(activities.get(activity), activities.get(ifTrue));
                Graph.Edge edge = graph.getEdge(activities.get(activity), activities.get(ifTrue));
                edge.setParam("color", "black");
                edge.setParam("label", "True");
                
                addEges(activities, ifTrue, graph);
                                                
                RemoteActivityDefinition ifFalse = ((RemoteConditionalActivityDefinition) activity).getNextActivityIfFalse();
                if (ifFalse == null)
                    return;
                
                if (graph.getEdge(activities.get(activity), activities.get(ifFalse)) != null)                
                    return;
                
                graph.addEdge(activities.get(activity), activities.get(ifFalse));
                edge = graph.getEdge(activities.get(activity), activities.get(ifFalse));
                edge.setParam("color", "black");
                edge.setParam("label", "False");
                
                addEges(activities, ifFalse, graph);
                                
            } else {
                RemoteActivityDefinition nextActivity = activity.getNextActivity();
                
                if (nextActivity == null)
                    return;
                
                if (graph.getEdge(activities.get(activity), activities.get(nextActivity)) != null)
                    return;
                
                graph.addEdge(activities.get(activity), activities.get(nextActivity));
                Graph.Edge edge = graph.getEdge(activities.get(activity), activities.get(nextActivity));
                edge.setParam("color", "black");
                
                addEges(activities, nextActivity, graph);
            }
        }
    }
        
    private void render() {
        getAllActivities(processDefinition.getStartActivity());
                
        HashMap<Graph.Node, RemoteActivityDefinition> nodes = new HashMap();
        HashMap<RemoteActivityDefinition, Graph.Node> activities = new HashMap();
                
        VizComponent vizComponent = new VizComponent();
        
        Graph graph = new Graph("G" + String.valueOf(processDefinition.getId()), Graph.DIGRAPH);
        
        for (RemoteActivityDefinition currentActivity : allActivities) {

            Graph.Node currentActivityNode = new Graph.Node(String.valueOf(currentActivity.getId()));
            currentActivityNode.setParam("label", "\"" + currentActivity.getName() + "\"");
            
            if (currentActivity.getType() == ActivityDefinition.TYPE_NORMAL)
                currentActivityNode.setParam("shape", "box");
            
            if (currentActivity.getType() == ActivityDefinition.TYPE_CONDITIONAL)
                currentActivityNode.setParam("shape", "diamond");
            
            nodes.put(currentActivityNode, currentActivity);
            activities.put(currentActivity, currentActivityNode);
        }
        addEges(activities, processDefinition.getStartActivity(), graph);
        
        vizComponent.setWidth("400px");
        vizComponent.setHeight("700px");
        vizComponent.drawGraph(graph);
        
        for (Graph.Node node : nodes.keySet())
            vizComponent.addCss(node, "stroke", "#000000");
        
        if (processInstance != null) {
            try {
                List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                        processInstance.getId(),
                        Page.getCurrent().getWebBrowser().getAddress(),
                        remoteSession.getSessionId());
                if (path != null) {
                    int pathSize = path.size();
                    for (int i = 0; i < pathSize; i += 1) {

                        Graph.Node b = activities.get(path.get(i));
                        
                        vizComponent.addCss(b, "fill", "#c0d5f7");
                        
                        if (i - 1 >= 0) {
                            Graph.Node a = activities.get(path.get(i - 1));
                            Graph.Edge edge = graph.getEdge(a, b);
                            edge.setParam("color", "red");
                        }
                    }
                }
            } catch (ServerSideException ex) {
            }
        }
        Label lblProcessName = new Label(processDefinition.getName());
        
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(lblProcessName);
        verticalLayout.addComponent(vizComponent);
        verticalLayout.setExpandRatio(vizComponent, 1);
        verticalLayout.setComponentAlignment(vizComponent, Alignment.MIDDLE_CENTER);
        verticalLayout.setComponentAlignment(lblProcessName, Alignment.MIDDLE_CENTER);
                
        setContent(verticalLayout);
    }
    
}
