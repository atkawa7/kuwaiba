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
package org.kuwaiba.web.modules.servmanager.views;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.views.AbstractView;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.web.procmanager.MiniAppPhysicalPath;
import org.kuwaiba.web.procmanager.MiniAppRackView;
import org.openide.util.Exceptions;

/**
 * Creates info tables from an inventory objects like Router, Switch, TributaryLink, etc 
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class TableCreator {
    /**
     * Possible table devices
     */
    private static final int ROUTER = 101;
    private static final int ODF = 103;
    private static final int ADM = 102;
    private static final int SWITCH = 104;
    private static final int PEERING = 105;
    private static final int EXTERNAL_EQUIPMENT = 111;
    /**
     * Link info
     */
    private static final int PROVIDER = 200;
    /**
     * MPLS link info
     */
    private static final int VC = 201;

    /**
     * Web service bean reference
     */
    private final WebserviceBean wsBean;
    /**
     * Service reference
     */
    private final RemoteObjectLight service;
    /**
     * IP address reference
     */
    private final String ipAddress;
    /**
     * Session id reference
     */
    private final String sessionId;
    
    public TableCreator(RemoteObjectLight service, WebserviceBean wsBean) {
        this.wsBean = wsBean;
        this.service = service;
        this.ipAddress = Page.getCurrent().getWebBrowser().getAddress();
        this.sessionId = ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId();
    }
    
    /**
     * Creates the title for the table
     * @param text title text
     * @param type which table is being created
     * @return a layout with the title and a style according with the type
     */
    private Component createTitle(String text, int type){
        HorizontalLayout lytTitle = new HorizontalLayout(new Label(text));
        lytTitle.addStyleName("device-title");
        switch(type){
            case ROUTER:
                lytTitle.addStyleName("router");
                break;
            case PEERING:  
                lytTitle.addStyleName("peering");
                break;
            case SWITCH:
                lytTitle.addStyleName("switch");
                break;
            case ODF:  
                lytTitle.addStyleName("odf");
                break;    
            case EXTERNAL_EQUIPMENT:  
                lytTitle.addStyleName("external_equipment");
                break;
            case ADM:  
                lytTitle.addStyleName("adm");
                break;   
            case PROVIDER:  
                lytTitle.addStyleName("provider");
                break;
            case VC:  
                lytTitle.addStyleName("vc");
                break;        
        }
        return lytTitle;
    }
    /**
     * Create a cell with a a short width
     * @param cell a cell
     * @return a cell with the right style sheets 
     */
    private Component createShortCell(Component cell){
        cell.removeStyleName("cell-with-border-normal-width");
        cell.addStyleName("cell-with-border-short-width");
        return cell; 
    }
    
    /**
     * Create a cell with a double width
     * @param cell a cell
     * @return a cell with the right style sheets 
     */
    private Component createExtraWidthCell(Component cell){
        cell.removeStyleName("cell-with-border-normal-width");
        cell.addStyleName("cell-with-border-long-width");
        return cell; 
    }
    
    /**
     * Creates a cell for the tables, because the cell in grid layout 
     * should be formating individually
     * @param value value to put in the cell
     * @return a formating layout ton insert in the grid layout cell
     */
    private Component createCell(Object value, boolean bold, boolean topBorder, boolean rightBorder, boolean noBottom){
        HorizontalLayout lytCell = new HorizontalLayout();
        lytCell.addStyleNames("cell-with-border");
        lytCell.addStyleNames("cell-with-border-normal-width");
        lytCell.addStyleNames("cell-with-border-bottom");
        if(bold)
            lytCell.addStyleName("cell-with-bold-text");
        if(rightBorder)    
            lytCell.addStyleName("cell-with-border-right");
        if(topBorder)
            lytCell.addStyleName("cell-with-border-top");
        if(noBottom){
            lytCell.removeStyleName("cell-with-border-bottom");
            lytCell.addStyleName("cell-with-border-left");
        }
        if(value instanceof String)
            lytCell.addComponent(new Label(((String)value).replace("\n", "<br>"), ContentMode.HTML));
        else if(value instanceof Button)
            lytCell.addComponent((Button)value);
        return lytCell;
    }
    
    /**
     * Retrieves the path to the need it icon for the table creation
     * @param icon which icon should be load
     * @return a string with the path to the img
     */
    private Component createIcon(String icon){
        Image image = new Image("", new ExternalResource("/report-icons/" + icon + ".png"));
        image.setWidth("90px");
        return image;
    }
    
    /**
     * Creates a table for a Router
     * @param objLight the given object
     * @param port the port where the link ends
     * @return a grid layout with the router's information
     * @throws ServerSideException if some attributes need it ot create the table couldn't be retrieved
     */
    public Component createRouter(RemoteObjectLight objLight, RemoteObjectLight port) throws ServerSideException{
        
        RemoteObject networkDevice = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        //Card
        String card = "";
        if(!port.getClassName().equals("Pseudowire")){
            List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), "GenericBoard", 
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            card = parents.get(parents.size() -1).getName();
        }
        
        String mmr = wsBean.getAttributeValueAsString(port.getClassName(), port.getId(), "meetmeroom", ipAddress, sessionId);
        String rmmr = wsBean.getAttributeValueAsString(port.getClassName(), port.getId(), "remotemeetmeroom", ipAddress, sessionId);
        
        RemoteObjectLight rack = wsBean.getFirstParentOfClass(objLight.getClassName(), objLight.getId(), "Rack",  Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        Button btnObjView = new Button("Object View");
        btnObjView.addStyleNames("v-button-link", "button-in-cell");
        
        Button rackBtn = new Button("Rack View");
        rackBtn.addStyleNames("v-button-link", "button-in-cell");
        
        
        if(rack != null){
            Properties properties = new Properties();
            properties.put("id", rack.getId());
            properties.put("className", "Rack");

            MiniAppRackView rackView = new MiniAppRackView(properties);
            rackView.setWebserviceBean(wsBean);
            rackBtn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                
                Component launchEmbedded = rackView.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                UI.getCurrent().addWindow(formWindow);
            });
            
            
            btnObjView.addClickListener(event -> {
                AbstractView objectViewInstance;
                try {
                    objectViewInstance = PersistenceService.getInstance().getViewFactory().
                            createViewInstance("org.kuwaiba.web.modules.navtree.views.ObjectView"); //NOI18N
                    objectViewInstance.buildWithBusinessObject(rack);
                    Window formWindow = new Window(" ");

                    Component launchEmbedded = rackView.launchEmbedded();
                    formWindow.setContent(objectViewInstance.getAsComponent());
                    formWindow.center();
                    formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                    formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                    UI.getCurrent().addWindow(formWindow);
                } catch (InstantiationException | InvalidArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
        
        String rackUnits = networkDevice.getAttribute("rackUnits");
        String rackPosition = networkDevice.getAttribute("position");
        String moreInformation = networkDevice.getAttribute("moreinformation");
        
        Properties properties = new Properties();
        properties.setProperty("id", port.getId());
        properties.setProperty("className", port.getClassName());
        
        MiniAppPhysicalPath physicalPath = new MiniAppPhysicalPath(properties);
        Button portBtn = new Button(port.getName());
        portBtn.addStyleNames("v-button-link", "button-in-cell");
        portBtn.addClickListener(event -> {
            Window formWindow = new Window(" ");
            Component launchEmbedded = physicalPath.launchEmbedded();
            formWindow.setContent(launchEmbedded);
            formWindow.center();
            UI.getCurrent().addWindow(formWindow);
        });

        boolean isFirstRow = true;
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
 
        lytData.addComponent(createTitle(objLight.getName(), ROUTER));
        
        if(!card.isEmpty()){
            Component cardRow = createTitleValueRow("CARD", card);
            cardRow.addStyleName("cell-with-border-top");
            lytData.addComponent(cardRow); 
            isFirstRow = false;
        }
         
        Component portRow = createTitleValueRow("PORT", portBtn);
        if(isFirstRow)
            portRow.addStyleName("cell-with-border-top");
        lytData.addComponent(portRow);        
        lytData.addComponent(createTitleValueRow(""));
        
        String hoster = getHoster(networkDevice);
        if(hoster != null && !hoster.isEmpty())
           lytData.addComponent(createTitleValueRow("DEVICE OWNER", hoster));

        String he = getHandE(networkDevice);
        if(he != null && !he.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE H&E", he));

        lytData.addComponent(createMergedCellsRow2(getLocation(rack, objLight)));
        lytData.addComponent(createTitleValueRow(""));
        
        if(rackPosition != null && isNumeric(rackPosition) && Integer.valueOf(rackPosition) > 0)
            lytData.addComponent(createTitleValueRow("RACK POSITION", (String)rackPosition));
        
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0)
            lytData.addComponent(createTitleValueRow("RACK UNITS", (String)rackUnits));
        
        if(mmr != null && !mmr.isEmpty())
            lytData.addComponent(createTitleValueRow("MMR", mmr));
        
        if(rmmr != null && !rmmr.isEmpty())
            lytData.addComponent(createTitleValueRow("RMMR", rmmr));

        if(moreInformation != null && !moreInformation.isEmpty())
            lytData.addComponent(createTitleValueRow("MORE INFO", moreInformation));
        //we add the device icon at the bottom of the table
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(objLight.getClassName()));
        lytIcon.addStyleName("device-icon-container");
        
        VerticalLayout lytRouterTable = new VerticalLayout(lytData, lytIcon);   
        lytRouterTable.setSpacing(false);

        return lytRouterTable;
    }
    
    private Component createMergedCellsRow(Object ...values){
        VerticalLayout lytRow = new VerticalLayout();
        lytRow.addStyleName("row");
        lytRow.setSpacing(false);
        boolean title = true;
        
        for(Object value : values){
            if(value instanceof String){
                Label label = new Label(((String)value).replace("\n", "<br>"), ContentMode.HTML);
                if(title){
                    label.addStyleNames("cell-with-bold-text");
                    title = false;
                }
                lytRow.addComponent(label);
            }
        }
        return lytRow;
    }
    
     private Component createMergedCellsRow2(List<Object> values){
        VerticalLayout lytRow = new VerticalLayout();
        lytRow.addStyleName("row");
        lytRow.setSpacing(false);
        boolean title = true;
        
        for(Object value : values){
            if(value instanceof String){
                Label label = new Label(("&nbsp;&nbsp;&nbsp;&nbsp;" + (String)value).replace("\n", "<br>"), ContentMode.HTML);
                if(title){
                    label.addStyleNames("cell-with-bold-text");
                    title = false;
                }
                lytRow.addComponent(label);
            }
            else if(value instanceof Button)
                lytRow.addComponent((Button) value);
        }
        return lytRow;
    }
    
    private Component createTitleValueRow(Object ...values){
        HorizontalLayout lytRow = new HorizontalLayout();
        lytRow.addStyleName("row");
        lytRow.setSpacing(false);
        boolean isFirst = true;
        boolean odd = true;
        
        for(Object value : values){
           if(value instanceof String && ((String)value).isEmpty()){
                lytRow.addStyleName("empty-row");
                return lytRow;
            }
            HorizontalLayout lytCell = createCell(value);
            //title right side
            if(odd){
                lytCell.addStyleName("cell-with-bold-text");
                odd = false;
                if(!isFirst)
                    lytCell.addStyleName("cell-with-border-left");
            }//value left side
            else{
                lytCell.addStyleName("cell-with-border-left");
                odd = true;
                isFirst = false;
            }
           
           lytRow.addComponent(lytCell);
        }
        return lytRow;
    }
    
    private Component createRow(Object ...values){
        HorizontalLayout lytRow = new HorizontalLayout();
        lytRow.addStyleName("row");
        lytRow.setSpacing(false);
        boolean odd = true;
        for(Object value : values){
            HorizontalLayout lytCell = createCell(value);
            lytCell.addStyleName("cell-test");
            if(odd)
                odd = false;
            else{
                lytCell.addStyleName("cell-with-border-left");
                odd = true;
            }
           lytRow.addComponent(lytCell);
        }
        return lytRow;
    }
    
    private HorizontalLayout createCell(Object value){
         HorizontalLayout lytCell = new HorizontalLayout();
            lytCell.addStyleNames("cell" ,"cell-normal");
            if(value == null)
                value = "-";
            if(value instanceof String){
                if(((String)value).length() == 2){
                    lytCell.removeStyleName("cell-normal");
                    lytCell.addStyleNames("cell" ,"cell-short");
                }
                lytCell.addComponent(new Label(((String)value).replace("\n", "<br>"), ContentMode.HTML));
            }
            else if(value instanceof Button)
                lytCell.addComponent((Button)value);
            return lytCell;
    }
    /**
     * Creates a table for a Peering
     * @param objLight the given object
     * @return a grid layout with the peering's information
     * @throws ServerSideException if some attributes need it ot create the table couldn't be retrieved
     */
    public Component createPeering(RemoteObjectLight objLight) throws ServerSideException{
        RemoteObject obj = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

        String peeringIp = obj.getAttribute("PeeringIP");
        String providerASN = obj.getAttribute("ProviderASN");
        String circuitId = obj.getAttribute("CircuitID");
        String providerCircuitId = obj.getAttribute("ProviderCircuitID");
        boolean isFirstRow = true;
        //values
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(objLight.getName(), PEERING));
        //first row
        if(peeringIp!= null && !peeringIp.isEmpty()){
            Component ipPeeringRow = createTitleValueRow("IP PEERING", peeringIp);
            ipPeeringRow.addStyleName("cell-with-border-top");
            lytData.addComponent(ipPeeringRow);
            lytData.addComponent(createTitleValueRow(""));
            isFirstRow = false;
        }
        
        if(circuitId != null && !circuitId.isEmpty()){
            Component circuitIdRow = createTitleValueRow("CIRCUIT ID", circuitId);
            if(isFirstRow){
                circuitIdRow.addStyleName("cell-with-border-top");
                isFirstRow = false;
            }
            lytData.addComponent(circuitIdRow);
        }
        
        if(providerCircuitId != null && !providerCircuitId.isEmpty()){
            Component providerCircuitIdRow = createTitleValueRow("INTERNAL ID", providerCircuitId);
            if(isFirstRow){
                providerCircuitIdRow.addStyleName("cell-with-border-top");
                isFirstRow = false;
            }
            lytData.addComponent(providerCircuitIdRow);
        }
        if(providerASN != null && !providerASN.isEmpty()){
            Component providerASNRow = createTitleValueRow("ASN NUMBER", providerASN);
            if(isFirstRow)
                providerASNRow.addStyleName("cell-with-border-top");
            lytData.addComponent(providerASNRow);
        }
        //Provider Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(objLight.getClassName()));
        lytIcon.addStyleName("device-icon-container");
        //Table
        VerticalLayout lytPeeringTable = new VerticalLayout(lytData, lytIcon);   
        lytPeeringTable.setSpacing(false);
        
        return lytPeeringTable;
    }
    
    /**
     * Creates a table for an ADM
     * @param objLight the given object
     * @param port the port where the link endsstmEndPoint@param stm used to calculate the cross connection
     * @param stmEndPoint
     * @return a grid layout with the ADM's information
     * @throws ServerSideException if one attribute need it to create the table couldn't be retrieved 
     */
    public Component createADM(RemoteObjectLight objLight, RemoteObjectLight port, RemoteObjectLight stmEndPoint) throws ServerSideException{
        RemoteObject obj = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        String rackUnits = obj.getAttribute("rackUnits");
        String rackPosition = obj.getAttribute("position");
        
        RemoteObjectLight card1 = null, card2 = null, port1 =null, port2 = null;
        
        RemoteObjectSpecialRelationships specialAttributes = wsBean.getSpecialAttributes(port.getClassName(), port.getId(), ipAddress, sessionId);
        List<String> relationships = specialAttributes.getRelationships();
        
        Button rackBtn = new Button("Rack View");
        rackBtn.addStyleNames("v-button-link", "button-in-cell");
        RemoteObjectLight rack = wsBean.getFirstParentOfClass(objLight.getClassName(), objLight.getId(), "Rack",  Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        if(rack != null){
            Properties properties = new Properties();
            properties.put("id", rack.getId());
            properties.put("className", "Rack");

            MiniAppRackView rackView = new MiniAppRackView(properties);
            rackView.setWebserviceBean(wsBean);
            rackBtn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                
                Component launchEmbedded = rackView.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                UI.getCurrent().addWindow(formWindow);
            });
        }

        //the X cross conection
        for(int i=0; i<relationships.size(); i++){
            if(relationships.get(i).equals("endpointA") || relationships.get(i).equals("endpointB")){
                if(relationships.get(i).equals("endpointA")){
                    port1 = port;
                    card1 = wsBean.getParentsUntilFirstOfClass(port1.getClassName(), 
                            port.getId(), "GenericBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                    
                    if(stmEndPoint != null){
                        port2 = stmEndPoint;
                        card2 = wsBean.getParentsUntilFirstOfClass(port2.getClassName(), port2.getId(), "GenericBoard", 
                                Page.getCurrent().getWebBrowser().getAddress(),
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                    }
                }
            
                else if(relationships.get(i).equals("endpointB")){
                    port2 = port;
                    card2 = wsBean.getParentsUntilFirstOfClass(port2.getClassName(), port2.getId(), "GenericBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                    if(stmEndPoint != null){
                        port1 = stmEndPoint;
                        card1 = wsBean.getParentsUntilFirstOfClass(port1.getClassName(), port1.getId(), "GenericBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                    }
                }
            }
        }
        
        String mmr = null, rmmr = null, mmr2 = null, rmmr2 = null;
        Button port1Btn = null, port2Btn = null;
        Properties properties = new Properties();
         //values
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(objLight.getName(), ADM));
        //cards title
        Component titleRow = createRow("CARD1", "CARD2");
        titleRow.addStyleNames("cell-with-border-top", "cell-with-bold-text");
        lytData.addComponent(titleRow);
        //row cadrs values
        lytData.addComponent(createRow(card1 != null ? card1.getName() : "", card2 != null ? card2.getName() : ""));
        
        if(port1 != null){
            mmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "meetmeroom", ipAddress, sessionId);
            rmmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "remotemeetmeroom", ipAddress, sessionId);
           
            properties.setProperty("id", port1.getId());
            properties.setProperty("className", port1.getClassName());

            MiniAppPhysicalPath physicalPath = new MiniAppPhysicalPath(properties);
            port1Btn = new Button(port1.getName());
            port1Btn.addStyleNames("v-button-link", "button-in-cell");
            port1Btn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                Component launchEmbedded = physicalPath.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                UI.getCurrent().addWindow(formWindow);
            });
        }
        if(port2 != null){
            mmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "meetmeroom", ipAddress, sessionId);
            rmmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "remotemeetmeroom", ipAddress, sessionId);
            //values
            properties.setProperty("id", port2.getId());
            properties.setProperty("className", port2.getClassName());

            MiniAppPhysicalPath physicalPath2 = new MiniAppPhysicalPath(properties);
            port2Btn = new Button(port2.getName());
            port2Btn.addStyleNames("v-button-link", "button-in-cell");
            port2Btn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                Component launchEmbedded = physicalPath2.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                UI.getCurrent().addWindow(formWindow);
            });
        }
        //Ports title
        titleRow = createRow("PORT1", "PORT2");
        titleRow.addStyleNames("cell-with-bold-text");
        lytData.addComponent(titleRow);
        //row
        lytData.addComponent(createRow(port1Btn != null ? port1Btn : "-", port2Btn != null ? port2Btn : "-"));
        if(mmr != null && !mmr.isEmpty() || rmmr != null && !rmmr.isEmpty()){
            //row
            titleRow = createRow("MMR", "RMMR");
            titleRow.addStyleNames("cell-with-bold-text");
            lytData.addComponent(titleRow);
            //row
            lytData.addComponent(createRow(mmr, rmmr));
        }
        if(mmr2 != null && !mmr2.isEmpty() || rmmr2 != null && !rmmr2.isEmpty()){
            //row
            titleRow = createRow("RMMR", "MMR");
            titleRow.addStyleNames("cell-with-bold-text");
            lytData.addComponent(titleRow);
            //row
            lytData.addComponent(createTitleValueRow(mmr2, rmmr2));
        }
        //row
        lytData.addComponent(createTitleValueRow(""));
        //row
        String hoster = getHoster(obj);
        if(hoster != null && !hoster.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE HOSTER", hoster));
        //row    
        String owner = getOwner(obj);
        if(owner != null && !owner.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE OWNER", owner));
        //row
        String he = getHandE(obj);
        if(he != null && !he.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE H&E", he));
        //row
        lytData.addComponent(createMergedCellsRow(getLocation(rack, objLight)));
        lytData.addComponent(createTitleValueRow(""));
        //row
        if(rackPosition != null && isNumeric(rackPosition) && Integer.valueOf(rackPosition) > 0)
            lytData.addComponent(createTitleValueRow("RACK POSITION", (String)rackPosition));
        //row
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0)
            lytData.addComponent(createTitleValueRow("RACK UNITS", (String)rackUnits));
        //ODF Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(objLight.getClassName()));
        lytIcon.addStyleName("device-icon-container");
        
        VerticalLayout lytADMTable = new VerticalLayout(lytData, lytIcon);   
        lytADMTable.setSpacing(false);
        return lytADMTable;
    }
    
    /**
     * Creates a table for a ODF
     * @param objLight the given object
     * @param port the port where the links ends
     * @return a grid layout with the ODF's information
     * @throws ServerSideException if one of the attributes need it to create the table couldn't be retrieved
     */
    public Component createODF(RemoteObjectLight objLight, RemoteObjectLight port) throws ServerSideException{
        
        RemoteObject odf = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        String rackPostion = odf.getAttribute("position");
        String rackUnits = odf.getAttribute("rackUnits");
        
        Button rackBtn = new Button("Rack View");
        rackBtn.addStyleNames("v-button-link", "button-in-cell");
        RemoteObjectLight rack = wsBean.getFirstParentOfClass(objLight.getClassName(), objLight.getId(), "Rack",  Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        if(rack != null){
            Properties properties = new Properties();
            properties.put("id", rack.getId());
            properties.put("className", "Rack");

            MiniAppRackView rackView = new MiniAppRackView(properties);
            rackView.setWebserviceBean(wsBean);
            rackBtn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                
                Component launchEmbedded = rackView.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                UI.getCurrent().addWindow(formWindow);
            });
        }
        
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(objLight.getName(), ODF));
        Component portRow = createTitleValueRow("ODF-PORT", port.getName());
        portRow.addStyleName("cell-with-border-top");
        lytData.addComponent(portRow);
        
        //row
        lytData.addComponent(createTitleValueRow(""));
        //row
        if(rackPostion != null && isNumeric(rackPostion) && Integer.valueOf(rackPostion) > 0)
            lytData.addComponent(createTitleValueRow("RACK POSTION", (String)rackPostion));
        //row
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0)
            lytData.addComponent(createTitleValueRow("RACK UNITS", (String)rackUnits));
        //row
        lytData.addComponent(createTitleValueRow(""));
        //row
        lytData.addComponent(createMergedCellsRow2(getLocation(rack, objLight)));
        //ODF Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(objLight.getClassName()));
        lytIcon.addStyleName("device-icon-container");
        
        VerticalLayout lytODFTable = new VerticalLayout(lytData, lytIcon);   
        lytODFTable.setSpacing(false);
        
        return lytODFTable;
    }
    
    /**
     * Creates a table for an X-Connection
     * @param objLight the given object
     * @param port the port where the links ends
     * @return a grid layout with the ODF's information
     * @throws ServerSideException if one of the attributes need it to create the table couldn't be retrieved
     */
    public Component createXconection(RemoteObjectLight objLight, RemoteObjectLight port) throws ServerSideException{
        
        RemoteObject odf = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        String rackPostion = odf.getAttribute("position");
        String rackUnits = odf.getAttribute("rackUnits");
        
        Button rackBtn = new Button("Rack View");
        rackBtn.addStyleNames("v-button-link", "button-in-cell");
        RemoteObjectLight rack = wsBean.getFirstParentOfClass(objLight.getClassName(), objLight.getId(), "Rack",  Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        if(rack != null){
            Properties properties = new Properties();
            properties.put("id", rack.getId());
            properties.put("className", "Rack");

            MiniAppRackView rackView = new MiniAppRackView(properties);
            rackView.setWebserviceBean(wsBean);
            rackBtn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                
                Component launchEmbedded = rackView.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                UI.getCurrent().addWindow(formWindow);
            });
        }
        
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(objLight.getName(), ODF));
        Component portRow = createTitleValueRow("ODF-PORT", port.getName());
        portRow.addStyleName("cell-with-border-top");
        lytData.addComponent(portRow);
        
        //row
        lytData.addComponent(createTitleValueRow(""));
        //row
        if(rackPostion != null && isNumeric(rackPostion) && Integer.valueOf(rackPostion) > 0)
            lytData.addComponent(createTitleValueRow("RACK POSTION", (String)rackPostion));
        //row
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0)
            lytData.addComponent(createTitleValueRow("RACK UNITS", (String)rackUnits));
        //row
        lytData.addComponent(createTitleValueRow(""));
        //row
        lytData.addComponent(createMergedCellsRow2(getLocation(rack, objLight)));
        //ODF Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(objLight.getClassName()));
        lytIcon.addStyleName("device-icon-container");
        
        VerticalLayout lytXconnectionTable = new VerticalLayout(lytData, lytIcon);   
        lytXconnectionTable.setSpacing(false);
        
        return lytXconnectionTable;
    }
    
    /**
     * Creates a table for a provider
     * @param providerName the provider name
     * @param providerId the provider id
     * @param legalOwner the legal owner
     * @return a grid layout with the provider's information
     */
    public Component createProviderTable(String providerName, String providerId, String legalOwner){
        //values
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(providerName, PROVIDER));
        //first row
        Component providerRow = createTitleValueRow("PROVIDER ID", providerId);
        providerRow.addStyleName("cell-with-border-top");
        lytData.addComponent(providerRow);
        
        if(legalOwner != null && !legalOwner.isEmpty())
            lytData.addComponent(createTitleValueRow("LEGAL OWNER", legalOwner));
        //Provider Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(providerName.toLowerCase()));
        lytIcon.addStyleName("device-icon-container");
        //Table
        VerticalLayout lytProviderTable = new VerticalLayout(lytData, lytIcon);   
        lytProviderTable.setSpacing(false);
        
        return lytProviderTable;
    }

    /**
     * Creates a table for a providers (submarine cable)
     * @param provider the given object
     * @return a grid layout with the router's information
     * @throws ServerSideException if some attributes need it to create the table could get retrieved
     */
    public Component createProviderTableS(RemoteObject provider) throws ServerSideException{
        String segment = "";
        //EuropeanNode or euNode
        String euNode = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "europeanNode",
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());//Listtype NodeType
        //EndNode
        String endNode = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "landingPoint", 
                Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());//Listtype NodeType
        
        String hop1Name = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "hop1Name", 
                Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        if(hop1Name.toLowerCase().equals("ace"))
            segment = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "aceSegment", ipAddress, sessionId);
        else if(hop1Name.toLowerCase().equals("wacs"))
            segment = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "wacsSegment", ipAddress, sessionId);
            
        String carfNumber = provider.getAttribute("hopCarf"); //listType ProviderType
        String moreInformation = provider.getAttribute("moreInformation");
        String hop1Id = provider.getAttribute("hop1Id");
        
        String hop1LegalOwner = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "hop1LegalOwner", 
                Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        boolean isFirstRow = true;
        //values
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(hop1Name, PROVIDER));
        
        if(hop1LegalOwner != null && !hop1LegalOwner.isEmpty()){
            Component legalOwneRow = createTitleValueRow("LEGAL OWNER", hop1LegalOwner);
            legalOwneRow.addStyleName("cell-with-border-top");
            lytData.addComponent(legalOwneRow);
            isFirstRow = false;
        }
        if(hop1Id != null && !hop1Id.isEmpty()){
            Component row = createTitleValueRow("PROVIDER ID", hop1Id);
            if(isFirstRow)
                row.addStyleName("cell-with-border-top");
            lytData.addComponent(row);
        }
        if(carfNumber != null && !carfNumber.isEmpty()){
            Component row = createTitleValueRow("CARF NUMBER", carfNumber);
            if(isFirstRow)
                row.addStyleName("cell-with-border-top");
            lytData.addComponent(row);
        }
        if(euNode != null && !euNode.isEmpty()){
            Component row = createTitleValueRow("EUROPEAN NODE", euNode);
            if(isFirstRow)
                row.addStyleName("cell-with-border-top");
            lytData.addComponent(row);
        }
        if(endNode != null && !endNode.isEmpty()){
            Component row = createTitleValueRow("LANDING NODE", endNode);
            if(isFirstRow)
                row.addStyleName("cell-with-border-top");
            lytData.addComponent(row);
        }
        if(segment != null && !segment.isEmpty()){
            Component row = createTitleValueRow("SEGMENT", segment);
            if(isFirstRow)
                row.addStyleName("cell-with-border-top");
            lytData.addComponent(row);
        }
        if(moreInformation != null && !moreInformation.isEmpty()){
            Component row = createTitleValueRow("MORE INFO", moreInformation);
            if(isFirstRow)
                row.addStyleName("cell-with-border-top");
            lytData.addComponent(row);
        }
        //Provider Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(hop1Name.toLowerCase()));
        lytIcon.addStyleName("device-icon-container");
        //Table
        VerticalLayout lytProviderTable = new VerticalLayout(lytData, lytIcon);   
        lytProviderTable.setSpacing(false);    
        
        return lytProviderTable;
    }
    
//    public Component createVC(RemoteObjectLight vcMplsLink) throws ServerSideException{
//        RemoteObjectLight sideA = wsBean.getSpecialAttribute(vcMplsLink.getClassName(), vcMplsLink.getId(), "mplsEndpointA", ipAddress, sessionId).get(0);
//        RemoteObjectLight sideB = wsBean.getSpecialAttribute(vcMplsLink.getClassName(), vcMplsLink.getId(), "mplsEndpointB", ipAddress, sessionId).get(0);
//        if(sideA != null && sideB != null)
//            return createVC(vcMplsLink, sideA, sideB);
//        else 
//            throw new ServerSideException("Could not determine the end point of the MPLS Link");
//    }
    /**
     * Creates a table for a VC (MPLSLinks)
     * @param vcMPLSLink the given object in this case a MPLSLink
     * @param sideA virtual port side A
     * @param sideB virtual port side B
     * @return a grid layout with the vc's information
     * @throws org.kuwaiba.exceptions.ServerSideException could not find the attribute
     */
    public Component createVC(RemoteObjectLight vcMPLSLink, RemoteObjectLight sideA, RemoteObjectLight sideB) throws ServerSideException{
        
        String ipSource = wsBean.getAttributeValueAsString(vcMPLSLink.getClassName(), vcMPLSLink.getId(), "ipSource", ipAddress, sessionId);
        String ipDestiny = wsBean.getAttributeValueAsString(vcMPLSLink.getClassName(), vcMPLSLink.getId(), "ipDestiny", ipAddress, sessionId);
        boolean isFirstRow = true;
        //values
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(vcMPLSLink.getName(), VC));
        
        if(sideA != null && sideB != null){
            Component pwRow = createTitleValueRow(
                    "PW", !sideA.getName().isEmpty() ? sideA.getName() : "-", 
                    "PW", !sideB.getName().isEmpty() ? sideB.getName() : "-");
            pwRow.addStyleName("cell-with-border-top");
            lytData.addComponent(pwRow);
            isFirstRow = false;
        }
        
        if(ipSource!= null && !ipSource.isEmpty() && ipDestiny != null && !ipDestiny.isEmpty()){
            Component ipsRow = createTitleValueRow(
                    "IP" , !ipSource.isEmpty() ? ipSource : "-", 
                    "IP" , !ipDestiny.isEmpty() ? ipDestiny : "-");
            if(isFirstRow)
                ipsRow.addStyleName("cell-with-border-top");
            lytData.addComponent(ipsRow);
        }
        VerticalLayout lytIcon =  new VerticalLayout();
        lytIcon.addStyleName("device-icon-container");
        //Table
        VerticalLayout lytVcTable = new VerticalLayout(lytData, lytIcon);   
        lytVcTable.setSpacing(false);

        return lytVcTable;
    }
    
    /**
     * Creates a table for a Switch
     * @param objLight the given object
     * @param port port of the physical link
     * @return a grid layout with the switch's information
     * @throws org.kuwaiba.exceptions.ServerSideException
     */
    public Component createSwitch(RemoteObjectLight objLight, RemoteObjectLight port) throws ServerSideException{

        RemoteObject switch_ = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        String rackPostion = switch_.getAttribute("position");
        String rackUnits = switch_.getAttribute("rackUnits");
        
        Button rackBtn = new Button("Rack View");
        rackBtn.addStyleNames("v-button-link", "button-in-cell");
        RemoteObjectLight rack = wsBean.getFirstParentOfClass(objLight.getClassName(), objLight.getId(), "Rack",  Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        if(rack != null){
            Properties properties = new Properties();
            properties.put("id", rack.getId());
            properties.put("className", "Rack");

            MiniAppRackView rackView = new MiniAppRackView(properties);
            rackView.setWebserviceBean(wsBean);
            rackBtn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                
                Component launchEmbedded = rackView.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                UI.getCurrent().addWindow(formWindow);
            });
        }

        //Card
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), "GenericBoard", 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        String card = "";
        if(!parents.isEmpty() && parents.get(0).getClassName().toLowerCase().contains("switch"))
            card = parents.get(parents.size() -1).getName();
        
        String mmr = wsBean.getAttributeValueAsString(port.getClassName(), port.getId(), "meetmeroom", ipAddress, sessionId);
        Properties properties = new Properties();
        properties.setProperty("id", port.getId());
        properties.setProperty("className", port.getClassName());
       
        MiniAppPhysicalPath physicalPath = new MiniAppPhysicalPath(properties);
        Button btnPort = new Button(port.getName());
        btnPort.addStyleNames("v-button-link", "button-in-cell");
        btnPort.addClickListener(event -> {
            Window formWindow = new Window(" ");
            Component launchEmbedded = physicalPath.launchEmbedded();
            formWindow.setContent(launchEmbedded);
            formWindow.center();
            UI.getCurrent().addWindow(formWindow);
        });
        boolean isFirstRow = true;
        //values
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(objLight.getName(), SWITCH));
        
        if(card != null && !card.isEmpty()){
            Component cardRow = createTitleValueRow("CARD", card);
            cardRow.addStyleName("cell-with-border-top");
            lytData.addComponent(cardRow);
            isFirstRow =false;
        }
        Component portRow = createTitleValueRow("PORT", btnPort);
        if(isFirstRow)
            portRow.addStyleName("cell-with-border-top");
        lytData.addComponent(portRow);
        lytData.addComponent(createTitleValueRow(""));
        //row
        String hoster = getHoster(objLight);
        if(hoster != null && !hoster.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE HOSTER", hoster));
        //row
        String owner = getHoster(objLight);
        if(owner != null && !owner.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE OWNER" , owner));      
        //row
        String he = getHandE(objLight);
        if(he != null && !he.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE H&E" , he));
        //row
        if(rackPostion != null && isNumeric(rackPostion) && Integer.valueOf(rackPostion) > 0)
            lytData.addComponent(createTitleValueRow("RACK POSITION" , (String)rackPostion));
        //row
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0)
            lytData.addComponent(createTitleValueRow("RACK UNNITS" , (String)rackUnits));

        //row
        if(mmr != null && !mmr.isEmpty())
            lytData.addComponent(createTitleValueRow("MMR", mmr));
        //row
        lytData.addComponent(createMergedCellsRow(getLocation(rack, objLight)));
        //Provider Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(objLight.getClassName()));
        lytIcon.addStyleName("device-icon-container");
        //Table data + icon
        VerticalLayout lytSwitchTable = new VerticalLayout(lytData, lytIcon);   
        lytSwitchTable.setSpacing(false);  

        return lytSwitchTable;
    }

    /**
     * Creates a table for an external equipment
     * @param objLight the given object
     * @return a grid layout with the external equipment's information
     * @throws ServerSideException if an attribute need it to create the table could get retrieved 
     */
    public Component createExternalEquipment(RemoteObjectLight objLight) throws ServerSideException{
        //values
        VerticalLayout lytData = new VerticalLayout();
        lytData.setSpacing(false);
        lytData.addStyleName("report-data-container");
        lytData.addComponent(createTitle(objLight.getName(), EXTERNAL_EQUIPMENT));
        
        RemoteObjectLight rack = wsBean.getFirstParentOfClass(objLight.getClassName(), objLight.getId(), "Rack",  Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
         
        Component deviceLocationRow = createMergedCellsRow(getLocation(rack, objLight));
        deviceLocationRow.addStyleName("cell-with-border-top");
        //first Row
        lytData.addComponent(deviceLocationRow);
        //Row
        String owner = getOwner(objLight);
        if(owner != null && !owner.isEmpty())
            lytData.addComponent(createTitleValueRow("DEVICE OWNER", owner));
        //Provider Icon
        VerticalLayout lytIcon =  new VerticalLayout(createIcon(objLight.getClassName()));
        lytIcon.addStyleName("device-icon-container");
        //Table
        VerticalLayout lytExternalEquipmentTable = new VerticalLayout(lytData, lytIcon);   
        lytExternalEquipmentTable.setSpacing(false);    
        
        return lytExternalEquipmentTable;
    }
    /**
     * Creates the location of a given object until the City
     * @param objLight the given object
     * @return a string with the location
     * @throws ServerSideException if the parents could no be calculated
     */
    private List<Object> getLocation(RemoteObjectLight rack, RemoteObjectLight objLight) throws ServerSideException{
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(objLight.getClassName(), objLight.getId(), "City",
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        String x = ">";
        List<Object> location = new ArrayList<>();
        location.add("DEVICE LOCATION");
        
        HorizontalLayout chooseView = new HorizontalLayout();
        chooseView.setSpacing(true);
        chooseView.setMargin(true);
        chooseView.setSizeFull();
        if(rack != null){
            //rack view
            Button btnRackView = new Button("Rack View");
            Properties properties = new Properties();
             properties.put("id", rack.getId());
            properties.put("className", "Rack");
            MiniAppRackView rackView = new MiniAppRackView(properties);
            rackView.setWebserviceBean(wsBean);

            btnRackView.addClickListener(event -> {
                    Window formWindow = new Window(" ");

                    Component launchEmbedded = rackView.launchEmbedded();
                    formWindow.setContent(launchEmbedded);
                    formWindow.center();
                    formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                    formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                    UI.getCurrent().addWindow(formWindow);
                });
            Button btnObjView = new Button("Object View");
            btnObjView.addClickListener(event -> {
                AbstractView objectViewInstance;
                try {
                    objectViewInstance = PersistenceService.getInstance().getViewFactory().
                            createViewInstance("org.kuwaiba.web.modules.navtree.views.ObjectView"); //NOI18N
                    objectViewInstance.buildWithBusinessObject(rack);
                    Window formWindow = new Window(" ");

                    formWindow.setContent(objectViewInstance.getAsComponent());
                    formWindow.center();
                    formWindow.setHeight(70, Sizeable.Unit.PERCENTAGE);
                    formWindow.setWidth(70, Sizeable.Unit.PERCENTAGE);
                    UI.getCurrent().addWindow(formWindow);
                } catch (InstantiationException | InvalidArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            chooseView.addComponents(btnRackView , btnObjView);
        }
        boolean viewsAdded = false;
        
        for (RemoteObjectLight parent : parents){
            if(rack != null && !viewsAdded){
                Button chooseViewBtn = new Button(x + parent.getName());
                chooseViewBtn.addStyleNames("v-button-link", "button-in-cell");
                chooseViewBtn.addClickListener(event -> {
                    Window formWindow = new Window("Choose a View");
                    formWindow.addStyleName("v-window");
                    formWindow.setContent(chooseView);
                    formWindow.center();
                    
                    formWindow.setHeight(10, Sizeable.Unit.PERCENTAGE);
                    formWindow.setWidth(20, Sizeable.Unit.PERCENTAGE);
                    UI.getCurrent().addWindow(formWindow);
                });
                viewsAdded = true;
                location.add(chooseViewBtn);
            }else
                location.add(x + parent.getName() + "<br>");
            x += ">";
        }
        return location;
    }
    /**
     * Creates the location of a given object until the City
     * @param objLight the given object
     * @return a string with the location
     * @throws ServerSideException if the parents could no be calculated
     */
    private String getCityLocation(RemoteObjectLight objLight) throws ServerSideException{
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(objLight.getClassName(), objLight.getId(), "City",
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        return parents.get(parents.size() -1).getName();
    }
    
    private String getOwner(RemoteObjectLight obj) throws ServerSideException{
        return wsBean.getAttributeValueAsString(obj.getClassName(), obj.getId(), "LegalOwner",
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
    }
    
    private String getHoster(RemoteObjectLight obj) throws ServerSideException{
        return wsBean.getAttributeValueAsString(obj.getClassName(), obj.getId(), "Hoster",
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
    }
    
    private String getHandE(RemoteObjectLight obj) throws ServerSideException{
        return wsBean.getAttributeValueAsString(obj.getClassName(), obj.getId(), "handsandeyes", 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
    }
    
    public static boolean isNumeric(String str){  
        try {  
            Double.parseDouble(str);  
        }catch(NumberFormatException ex){  
          return false;  
        }  
        return true;  
    }
}
