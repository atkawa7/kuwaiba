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
package org.kuwaiba.web.processes;

import com.vaadin.cdi.CDIView;
import com.vaadin.contextmenu.ContextMenu;
import com.vaadin.contextmenu.Menu;
import com.vaadin.contextmenu.MenuItem;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.pontus.vizcomponent.VizComponent;
import com.vaadin.pontus.vizcomponent.VizComponent.NodeClickListener;
import com.vaadin.pontus.vizcomponent.model.Graph;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.Date;
import java.util.HashMap;
import javax.inject.Inject;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.elements.FormLoader;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.Actor;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteForm;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("processinstance")
public class ProcessInstanceView extends CustomComponent implements View {
    public static String VIEW_NAME = "processinstance";
    
    @Inject
    private WebserviceBeanLocal wsBean;
    
    private RemoteProcessDefinition initProcessDefinition() {
        // Actors
        RemoteActor commercial = new RemoteActor(1, "Kelly Rojas", Actor.TYPE_USER);
        RemoteActor engineering = new RemoteActor(2, "Leo", Actor.TYPE_USER);
        RemoteActor serviceDelivery  = new RemoteActor(3, "Luis", Actor.TYPE_USER);
        // Activity Start
        RemoteActivityDefinition startActivityDef = new RemoteActivityDefinition(1, "Inicio", "Inicio", ActivityDefinition.TYPE_START, null, commercial);
        
        RemoteProcessDefinition processDef = new RemoteProcessDefinition(1, "Alta de Servicio", "Alta de Servicio", new Date().getTime(), "1.0", true, startActivityDef);
        // Activity New Service Order
        RemoteArtifactDefinition newServiceOrderForm = new RemoteArtifactDefinition(2, "Service Order Form", "Service Order Form", "1.0", ArtifactDefinition.TYPE_FORM, null); //TODO: get form
                
        RemoteActivityDefinition newServiceOrder = new RemoteActivityDefinition(2, "Nueva Orden de Servicio", "Nueva Orden de Servicio", ActivityDefinition.TYPE_NORMAL, newServiceOrderForm, commercial);
        
        startActivityDef.setNextActivity(newServiceOrder);
        // Activity Equipment Selector
        RemoteArtifactDefinition equipmentSelectorForm = new RemoteArtifactDefinition(3, "Equipment Selector Form", "Equipment Selector Form", "1.0", ArtifactDefinition.TYPE_FORM, null); //TODO: get form
        
        RemoteActivityDefinition equipmentSelector = new RemoteActivityDefinition(3, "Selección de Equipos", "Equipment Selector", ActivityDefinition.TYPE_NORMAL, equipmentSelectorForm, engineering);
        
        newServiceOrder.setNextActivity(equipmentSelector);
        // Activity Equipment Assignment
        RemoteArtifactDefinition equipmentAssignmentForm = new RemoteArtifactDefinition(4, "Equipment Assignment Form", "Equipment Assignment Form", "1.0", ArtifactDefinition.TYPE_FORM, null); //TODO: get form
        
        RemoteActivityDefinition equipmentAssignment = new RemoteActivityDefinition(4, "Asignación de Equipos", "Equipment Assignment", ActivityDefinition.TYPE_NORMAL, equipmentAssignmentForm, serviceDelivery);
        
        equipmentSelector.setNextActivity(equipmentAssignment);
        // Actity Fin
        RemoteActivityDefinition endActivityDef = new RemoteActivityDefinition(5, "Fin", "Fin", ActivityDefinition.TYPE_END, null, commercial);
        
        equipmentAssignment.setNextActivity(endActivityDef);
        
        return processDef;        
    }
    
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
                
        HashMap<Graph.Node, RemoteActivityDefinition> nodes = new HashMap();
        HashMap<RemoteActivityDefinition, Graph.Node> activities = new HashMap();
        HashMap<Graph.Node, ContextMenu> contextMenus = new HashMap();
        
        VizComponent vizComponent = new VizComponent();

        // The User Select A Process
        // The App Load the Process
        // The User start the process
        RemoteProcessDefinition processDef = initProcessDefinition();  
                
        RemoteProcessInstance processInstance = new RemoteProcessInstance(1, "", "", 1, processDef.getId());
        
        Graph graph = new Graph("G" + String.valueOf(processDef.getId()), Graph.DIGRAPH);
        
        
        RemoteActivityDefinition currentActivity = processDef.getStartAction();
        while (true) {
            RemoteActivityDefinition nextActivity = currentActivity.getNextActivity();
            
            if (nextActivity == null)
                break;
            
            Graph.Node currentActivityNode = new Graph.Node(new Date().getTime() + String.valueOf(currentActivity.getId()));
            currentActivityNode.setParam("label", "\"" + currentActivity.getName() + "\"");
            
            Graph.Node nextActivityNode = new Graph.Node(new Date().getTime() + String.valueOf(nextActivity.getId()));
            nextActivityNode.setParam("label", "\"" + nextActivity.getName() + "\"");
            
            nodes.put(currentActivityNode, currentActivity);
            nodes.put(nextActivityNode, nextActivity);
            
            activities.put(currentActivity, currentActivityNode);
            activities.put(nextActivity, nextActivityNode);
                        
            ContextMenu currentActivityContextMenu = new ContextMenu(this, false);
            if (currentActivity.getArfifact() != null)
                currentActivityContextMenu.addItem("Artifact", new ArtifactMenu(currentActivity));
            
            currentActivityContextMenu.addItem("Commit", new ActivityMenu(processInstance, currentActivity, activities, vizComponent));
            contextMenus.put(currentActivityNode, currentActivityContextMenu);
            
            ContextMenu nextActivityContextMenu = new ContextMenu(this, false);
            if (nextActivity.getArfifact() != null)
                nextActivityContextMenu.addItem("Artifact", new ArtifactMenu(nextActivity));
            
            nextActivityContextMenu.addItem("Commit", new ActivityMenu(processInstance, nextActivity, activities, vizComponent));
            contextMenus.put(nextActivityNode, nextActivityContextMenu);
                                    
            graph.addEdge(currentActivityNode, nextActivityNode);
            Graph.Edge edge = graph.getEdge(currentActivityNode, nextActivityNode);
            
            if (currentActivity.getType() == ActivityDefinition.TYPE_NORMAL)
                currentActivityNode.setParam("shape", "box");
                
            if (nextActivity.getType() == ActivityDefinition.TYPE_NORMAL)
                nextActivityNode.setParam("shape", "box");                
                
            edge.setParam("color", "blue");
            
            currentActivity = nextActivity;                    
        }
        vizComponent.setWidth("400px");
        vizComponent.setHeight("700px");
        vizComponent.drawGraph(graph);
        
        for (Graph.Node node : nodes.keySet())
            vizComponent.addCss(node, "stroke", "#62c192");
        
        vizComponent.addCss(activities.get(processDef.getStartAction()), "stroke", "#f4a742");
        
        vizComponent.addClickListener(new NodeClickListener() {
            @Override
            public void nodeClicked(VizComponent.NodeClickEvent e) {
                
                Graph.Node node = e.getNode();
                                
                int x = e.getMouseEventDetails().getClientX();
                int y = e.getMouseEventDetails().getClientY();
                
                contextMenus.get(node).open(x, y);
            }
        });
                        
        Label lblProcessName = new Label(processDef.getName());
        
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(lblProcessName);
        verticalLayout.addComponent(vizComponent);
        verticalLayout.setExpandRatio(vizComponent, 1);
        verticalLayout.setComponentAlignment(vizComponent, Alignment.MIDDLE_CENTER);
        verticalLayout.setComponentAlignment(lblProcessName, Alignment.MIDDLE_CENTER);
                
        setCompositionRoot(verticalLayout);
    }    
    
    private class ArtifactMenu implements Menu.Command {
        private RemoteActivityDefinition activityDef;
        
        public ArtifactMenu(RemoteActivityDefinition activityDef) {
            this.activityDef = activityDef;
        }
        
        @Override
        public void menuSelected(MenuItem selectedItem) {

            try {
                String address = Page.getCurrent().getWebBrowser().getAddress();
                RemoteSession remoteSession = (RemoteSession) getSession().getAttribute("session");
                RemoteForm remoteForm = null;
                if (activityDef.getId() == 2)
                        remoteForm = wsBean.getForm(39750, address, remoteSession.getSessionId());
                if (activityDef.getId() == 3)
                    remoteForm = wsBean.getForm(39770, address, remoteSession.getSessionId());
                if (activityDef.getId() == 4)
                    remoteForm = wsBean.getForm(39790, address, remoteSession.getSessionId());
                if (remoteForm != null) {
                    //getUI().getNavigator().navigateTo(FormView.VIEW_NAME);

                    if (remoteForm.getStructure() == null)
                        return;

                    FormLoader formBuilder = new FormLoader(remoteForm.getStructure());            
                    formBuilder.build();

                    FormRenderer formRenderer = new FormRenderer(formBuilder);
                    formRenderer.render(wsBean, remoteSession);
                                    
                    Window subWindow = new Window(remoteForm.getName());
                    subWindow.setModal(true);

                    Panel pnlForm = new Panel();
                    pnlForm.setContent(formRenderer);
                    pnlForm.setSizeUndefined();
                    subWindow.setContent(pnlForm);
                    
                    subWindow.setResizable(true);
                    subWindow.center();

                    subWindow.setSizeFull();

                    UI.getCurrent().addWindow(subWindow);
                }
            } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    
    private class ActivityMenu implements Menu.Command {
        private RemoteProcessInstance processInstance;
        private VizComponent vizComponent;
        private RemoteActivityDefinition activityDef;
        private final HashMap<RemoteActivityDefinition, Graph.Node> activities;
        
        public ActivityMenu(RemoteProcessInstance processInstance, RemoteActivityDefinition activityDef, HashMap<RemoteActivityDefinition, Graph.Node> activities, VizComponent vizComponent) {
            this.processInstance = processInstance;
            this.activityDef = activityDef;
            this.vizComponent = vizComponent;
            this.activities = activities;
        }
        
        @Override
        public void menuSelected(MenuItem selectedItem) {                    
            if (getProcessInstance().getCurrentActivity() != activityDef.getId()) {
                Notification.show("Only the current activity can be committed", Notification.Type.ERROR_MESSAGE);
                return;
            }
            RemoteActivityDefinition nextActivity = getActivityDef().getNextActivity();
            
            if (nextActivity != null) {
                getProcessInstance().setCurrentActivity(nextActivity.getId());
                getVizComponent().addCss(activities.get(nextActivity), "stroke", "#f4a742");
            }
                        
            Notification.show("Activity was committed", Notification.Type.HUMANIZED_MESSAGE);
            
            getVizComponent().addCss(activities.get(activityDef), "stroke", "#ce5c7b");
        }
        
        public RemoteProcessInstance getProcessInstance() {
            return processInstance;
        }
        
        public void setProcessInstance(RemoteProcessInstance processInstance) {
            this.processInstance = processInstance;            
        }
        
        public VizComponent getVizComponent() {
            return vizComponent;
        }
        
        public void setVizComponent(VizComponent vizComponent) {
            this.vizComponent = vizComponent;
        }
        
        public RemoteActivityDefinition getActivityDef() {
            return activityDef;
        }
        
        public void setActivityDef(RemoteActivityDefinition activityDef) {
            this.activityDef = activityDef;
        }
    }
}
