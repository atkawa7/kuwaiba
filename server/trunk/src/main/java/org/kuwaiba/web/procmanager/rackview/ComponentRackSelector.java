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
package org.kuwaiba.web.procmanager.rackview;

import com.vaadin.data.HasValue;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteScriptQueryResultCollection;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.openide.util.Exceptions;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentRackSelector extends VerticalLayout {
    private final WebserviceBean webserviceBean;
    
    public ComponentRackSelector(ComponentDeviceList componentDeviceList, WebserviceBean webserviceBean/*, ComponentRackView componentRackView*/) {
        this.webserviceBean = webserviceBean;
        initializeComponent(componentDeviceList, webserviceBean/*, componentRackView*/);                        
    }
    
    private List<RemoteObjectLight> getItems(String parentClassName, long parentId, String childClassName) {
        
        try {
            List<StringPair> scriptQueryParameters = new ArrayList();
            scriptQueryParameters.add(new StringPair("parentId", String.valueOf(parentId)));
            scriptQueryParameters.add(new StringPair("parentClassName", parentClassName));
            scriptQueryParameters.add(new StringPair("childClassName", childClassName));
            
            webserviceBean.updateScriptQueryParameters(
                    "getObjectChildrenRecursive",
                    scriptQueryParameters,
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            RemoteScriptQueryResultCollection result = webserviceBean.executeScriptQueryCollection(
                    "getObjectChildrenRecursive",
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            return (List<RemoteObjectLight>) result.getResults();
            
        } catch (ServerSideException ex) {
            //Exceptions.printStackTrace(ex);
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
        return null;
    }
        
    private void initializeComponent(ComponentDeviceList componentDeviceList, WebserviceBean webserviceBean/*, ComponentRackView componentRackView*/) {
        setSizeFull();
        
        HorizontalLayout horizontalLayout = new HorizontalLayout();        
        horizontalLayout.setSpacing(false);
        
        horizontalLayout.setSizeFull();
                
        Panel leftPanel = new Panel("Devices");
                
        leftPanel.setContent(componentDeviceList);
                
        leftPanel.setSizeFull();
        
        Panel rightPanel = new Panel();
        
        Label lblCity = new Label("City");
        lblCity.addStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblBuilding = new Label("Building");
        lblBuilding.addStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblRack = new Label("Rack");
        lblRack.addStyleName(ValoTheme.LABEL_BOLD);
        
        ComboBox cmbCity = new ComboBox();
        ComboBox cmbBuildings = new ComboBox();
        ComboBox cmbRacks = new ComboBox();
        
        List<RemoteObjectLight> cities = new ArrayList();
        
        try {
            
            List<StringPair> scriptQueryParameters = new ArrayList();
            scriptQueryParameters.add(new StringPair("className", "City")); //NOI18N
                        
            webserviceBean.updateScriptQueryParameters(
                "getInstancesOfFinalClass", 
                scriptQueryParameters, 
                Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
            
            RemoteScriptQueryResultCollection result = webserviceBean.executeScriptQueryCollection(
                "getInstancesOfFinalClass", 
                Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
            
            cities = (List<RemoteObjectLight>) result.getResults();
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        cmbCity.setItems(cities);
        
        cmbCity.addValueChangeListener(new HasValue.ValueChangeListener<RemoteObjectLight>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteObjectLight> event) {
                if (event.getValue() != null && webserviceBean != null) {
                    List<RemoteObjectLight> buildings = getItems(event.getValue().getClassName(), event.getValue().getId(), "Building"); //NOI18N
                    cmbBuildings.setItems(buildings);
                    
                    List<RemoteObjectLight> racks = getItems(event.getValue().getClassName(), event.getValue().getId(), "Rack"); //NOI18N
                    cmbRacks.setItems(racks);
                }
            }
        });
        cmbBuildings.addValueChangeListener(new HasValue.ValueChangeListener<RemoteObjectLight>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteObjectLight> event) {
                
                if (event.getValue() != null && webserviceBean != null) {
                    List<RemoteObjectLight> racks = getItems(event.getValue().getClassName(), event.getValue().getId(), "Rack"); //NOI18N
                    cmbRacks.setItems(racks);
                }
            }
        });
        
        cmbRacks.addValueChangeListener(new HasValue.ValueChangeListener<RemoteObjectLight>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteObjectLight> event) {
                
                if (event.getValue() != null && webserviceBean != null)
                    rightPanel.setContent(new ComponentRackView(event.getValue(), webserviceBean));
            }
        });
        rightPanel.setSizeFull();
        
        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setSizeFull();
        
        GridLayout grdRack = new GridLayout();
        grdRack.setSizeFull();
        
        grdRack.setRows(1);
        grdRack.setColumns(6);
        grdRack.addComponent(lblCity);
        grdRack.addComponent(cmbCity);
        grdRack.addComponent(lblBuilding);
        grdRack.addComponent(cmbBuildings);
        grdRack.addComponent(lblRack);
        grdRack.addComponent(cmbRacks);
        
        grdRack.setComponentAlignment(lblCity, Alignment.MIDDLE_CENTER);
        grdRack.setComponentAlignment(lblBuilding, Alignment.MIDDLE_CENTER);
        grdRack.setComponentAlignment(lblRack, Alignment.MIDDLE_CENTER);
                
        rightVerticalLayout.addComponent(grdRack);
        rightVerticalLayout.addComponent(rightPanel);
        
        rightVerticalLayout.setExpandRatio(grdRack, 0.05f);
        rightVerticalLayout.setExpandRatio(rightPanel, 0.95f);
        
        rightVerticalLayout.setComponentAlignment(grdRack, Alignment.MIDDLE_CENTER);
                                        
        horizontalLayout.addComponent(leftPanel);
        horizontalLayout.addComponent(rightVerticalLayout);
        
        horizontalLayout.setExpandRatio(leftPanel, 0.40f);
        horizontalLayout.setExpandRatio(rightVerticalLayout, 0.60f);
                        
        addComponent(horizontalLayout);
    }   
}
