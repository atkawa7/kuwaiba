/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.web.modules.ltmanager.dashboard;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.properties.PropertyFactory;
import org.kuwaiba.apis.web.gui.properties.PropertySheet;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * The dashboard widget to be used as the main widget in the Navigation Tree dashboard
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class IPAddressManagerExplorerDashboardWidget extends AbstractDashboardWidget {
    /**
     * The property sheet that allows to edit a properties of the selected item in the nav tree
     */
    private PropertySheet propertySheet;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    /**
     * Reference to the business object to be explored
     */
    private RemoteObjectLight selectedObject;
    
    public IPAddressManagerExplorerDashboardWidget(RemoteObjectLight selectedObject, WebserviceBean wsBean) {
        super(String.format("Properties for %s", selectedObject));
        this.wsBean = wsBean;
        this.selectedObject = selectedObject;
        this.createContent();
        this.setSizeFull();
    }

    @Override
    public void createCover() { }

    @Override
    public void createContent() {
        VerticalLayout lytContent = new VerticalLayout();
        lytContent.setMargin(true);
        lytContent.setSizeFull();
        try {            
            this.propertySheet = new PropertySheet(PropertyFactory.propertiesFromRemoteObject(selectedObject, wsBean), title);
            
            if (wsBean.isSubclassOf(selectedObject.getClassName(), Constants.CLASS_IP_ADDRESS,  Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()))
                lytContent.addComponents(ipAddressDetails(), this.propertySheet);
            else
                lytContent.addComponents(subnetDetails(), this.propertySheet);
            
            this.contentComponent = lytContent;
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
        this.contentComponent = lytContent;
        addComponent(contentComponent);
    }
    
    /**
     * Shows detailed information about a subnet
     * @return A layout object containing the extra info
     */
    private VerticalLayout subnetDetails() {
        VerticalLayout lytSubnetDetails = new VerticalLayout();
        try {
            List<RemoteObjectLight> ipAddressesWithin = wsBean.getSpecialChildrenOfClassLight(selectedObject.getId(), selectedObject.getClassName(), 
                    "GenericAddress", -1,  Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            lytSubnetDetails.addComponent(new Label(String.format("<b>IP addresses in this subnet:</b> %s", ipAddressesWithin.size()), ContentMode.HTML));
            
            Grid<RemoteObjectLight> tblIpAddressesWithin = new Grid<>("IP Address and Subnets Inside this Subnet");
            tblIpAddressesWithin.setItems(ipAddressesWithin);
            tblIpAddressesWithin.addColumn(RemoteObjectLight::getName).setCaption("Name");
            
            lytSubnetDetails.addComponent(tblIpAddressesWithin);
            lytSubnetDetails.setSizeFull();
            
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
        
        return lytSubnetDetails;
    }
    
    /**
     * Shows detailed information about an IP address
     * @return A layout object containing the extra info
     */
    private VerticalLayout ipAddressDetails() {
        VerticalLayout lytIpAddressDetails = new VerticalLayout();
        try {
            HashMap<String, RemoteObjectLightList> specialAttributes = wsBean.getSpecialAttributes(selectedObject.getClassName(), selectedObject.getId(),  Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).asHashMap();
            
            if (specialAttributes.containsKey("ipamHasIpAddress")) {
                RemoteObjectLight relatedInterface = specialAttributes.get("ipamHasIpAddress").getList().get(0);
                RemoteObjectLight communicationsElement = wsBean.getFirstParentOfClass(relatedInterface.getClassName(), relatedInterface.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT,
                        Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
                lytIpAddressDetails.addComponent(new Label(String.format("<b>Assigned to:</b> %s &rarr; %s", 
                    communicationsElement == null ? "<No Communications Element>" : communicationsElement, relatedInterface), ContentMode.HTML));
            } else
                lytIpAddressDetails.addComponent(new Label("This address is not assigned to any interface"));
            
            String isManagementAddress = wsBean.getAttributeValueAsString(selectedObject.getClassName(), selectedObject.getId(), "isManagementAddress", Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            if (isManagementAddress != null && isManagementAddress.equals("true"))
                lytIpAddressDetails.addComponent(new Label("This address is used in a management interface"));
            else
                lytIpAddressDetails.addComponent(new Label("This address is not used in a management interface"));
            
            if (specialAttributes.containsKey("uses")) {
                Grid<RemoteObjectLight> tblServices = new Grid<>("Services Using this IP Address");
                tblServices.setItems(specialAttributes.get("uses").getList());
                tblServices.addColumn(RemoteObjectLight::getName).setCaption("Name");

                lytIpAddressDetails.addComponent(tblServices);
            } else
                lytIpAddressDetails.addComponent(new Label("This address is not associated to any service"));
            
            lytIpAddressDetails.setSizeUndefined();
            lytIpAddressDetails.setMargin(true);
            
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
        
        return lytIpAddressDetails;
    }
}
