/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.web.procmanager.MiniAppPhysicalPath;

/**
 * Creates info tables from an inventory objects like Router, Switch, TributaryLink, etc 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
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
        Image image = new Image("", new ExternalResource("/icons/" + icon + ".png"));
        image.setWidth("100px");
        image.addStyleNames("device-img");
        HorizontalLayout lytCell = new HorizontalLayout();
        lytCell.addStyleNames("cell-with-img");
        lytCell.addComponent(image);
        return lytCell;
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
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), "GenericBoard", 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        String card = parents.get(parents.size() -1).getName();
        
        String mmr = wsBean.getAttributeValueAsString(port.getClassName(), port.getId(), "meetmeroom", ipAddress, sessionId);
        String rmmr = wsBean.getAttributeValueAsString(port.getClassName(), port.getId(), "remotemeetmeroom", ipAddress, sessionId);
        String rackUnits = networkDevice.getAttribute("rackUnits");
        String rackPosition = networkDevice.getAttribute("position");
        String moreInformation = networkDevice.getAttribute("moreinformation");
        
        //We create the table with a grid layout
        GridLayout grdRouter = new GridLayout(2, 18);
        grdRouter.addStyleName("report-forms-box");
        
        grdRouter.addComponent(createTitle(objLight.getName(), ROUTER), 0, 0, 1, 0);
        
        grdRouter.addComponent(createCell("CARD", true, true, true, false), 0, 1);
        grdRouter.addComponent(createCell("PORT", true, true, false, false), 1, 1);
        
        Properties properties = new Properties();
        properties.setProperty("id", Long.toString(port.getId()));
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
        grdRouter.addComponent(createCell(card, false, false, true, false), 0, 2);
        grdRouter.addComponent(createCell(portBtn, false, false, false, false), 1, 2);
                
        grdRouter.addComponent(createCell(" ", false, false, false, false), 0, 3, 1, 3);
        String hoster = getHoster(networkDevice);
        if(hoster != null && !hoster.isEmpty()){
            grdRouter.addComponent(createCell("DEVICE HOSTER", true, false, true, false), 0, 5);
            grdRouter.addComponent(createCell(hoster , false, false, false, false), 1, 5);
        }
        String owner = getOwner(networkDevice);
        if(owner != null && !owner.isEmpty()){
            grdRouter.addComponent(createCell("DEVICE OWNER", true, false, true, false), 0, 6);
            grdRouter.addComponent(createCell(owner, false, false, false, false), 1, 6);
        }
        String he = getHandE(networkDevice);
        if(he != null && !he.isEmpty()){
            grdRouter.addComponent(createCell("DEVICE H&E", true, false, true, false), 0, 7);
            grdRouter.addComponent(createCell(he, false, false, false, false), 1, 7);
        }
        grdRouter.addComponent(createCell("DEVICE LOCATION", true, false, false, false), 0, 8, 1, 8);
        grdRouter.addComponent(createCell(getLocation(objLight), false, false, false, false), 0, 9, 1, 9);
        
        grdRouter.addComponent(createCell(" ", false, false, false, false), 0, 10, 1, 10);
        
        if(rackPosition != null && isNumeric(rackPosition) && Integer.valueOf(rackPosition) > 0){
            grdRouter.addComponent(createCell("RACK POSITION", true, false, true, false), 0, 11);
            grdRouter.addComponent(createCell(rackPosition, false, false, false, false), 1, 11);
        }
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0){
            grdRouter.addComponent(createCell("RACK UNITS", true, false, true, false), 0, 12);
            grdRouter.addComponent(createCell(rackUnits, false, false, false, false), 1, 12);
        }
        if(mmr != null && !mmr.isEmpty()){
            grdRouter.addComponent(createCell("MMR", true, false, true, false), 0, 13);
            grdRouter.addComponent(createCell(mmr, false, false, false, false), 1, 13);
        }
        if(rmmr != null && !rmmr.isEmpty()){
            grdRouter.addComponent(createCell("RMMR", true, false, true, false), 0, 14);
            grdRouter.addComponent(createCell(rmmr, false, false, false, false), 1, 14);
        }
        if(moreInformation != null && !moreInformation.isEmpty()){
            grdRouter.addComponent(createCell("MORE INFO", true, false, false, false), 0, 15, 1, 15);
            grdRouter.addComponent(createCell(moreInformation, false, false, false, false), 0, 16, 1, 16);
        }
        grdRouter.addComponent(createIcon(objLight.getClassName()), 0, 17, 1, 17);
        return grdRouter;
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
        String circuitID = obj.getAttribute("CircuitID");
        String providerCircuitID = obj.getAttribute("ProviderCircuitID");
        
        GridLayout grdPeering = new GridLayout(2, 7);
        grdPeering.addStyleName("report-forms-box");
        grdPeering.addComponent(createTitle(objLight.getName(), PEERING), 0, 0, 1, 0);
        
        if(peeringIp != null && !peeringIp.isEmpty()){
            grdPeering.addComponent(createCell("IP PEERING", true, true, true, false), 0, 1);
            grdPeering.addComponent(createCell(peeringIp, false, true, false, false), 1, 1);
        }
        grdPeering.addComponent(createCell(" ", false, false, false, false), 0, 2, 1, 2);
        if(circuitID != null && !circuitID.isEmpty()){
            grdPeering.addComponent(createCell("CIRCUIT ID", true, false, true, false), 0, 3);     
            grdPeering.addComponent(createCell(circuitID, false, false, false, false), 1, 3);        
        }
        if(providerCircuitID != null && !providerCircuitID.isEmpty()){
            grdPeering.addComponent(createCell("INTERNAL ID", true, false, true, false), 0, 4);
            grdPeering.addComponent(createCell(providerCircuitID, false, false, false, false), 1, 4);
        }
        if(providerASN != null && providerASN.isEmpty()){
            grdPeering.addComponent(createCell("ASN NUMBER", true, false, true, false), 0, 5);
            grdPeering.addComponent(createCell(providerASN, false, false, false, false), 1, 5);
        }
        grdPeering.addComponent(createIcon(objLight.getClassName()), 0, 6, 1, 6);
        return grdPeering;
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
        
        String mmr, rmmr, mmr2, rmmr2;
        Properties properties = new Properties();
        
        GridLayout grdADM = new GridLayout(2, 20);
        grdADM.addStyleName("report-forms-box");
        grdADM.addComponent(createTitle(objLight.getName(), ADM), 0, 0, 1, 0);
         
        if(port1 != null){
            mmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "meetmeroom", ipAddress, sessionId);
            rmmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "remotemeetmeroom", ipAddress, sessionId);
            //Column 1
            grdADM.addComponent(createCell("CARD 1", true, true, true, false), 0, 1);
            grdADM.addComponent(createCell("PORT 1", true, false, true, false), 0, 3);

            properties.setProperty("id", Long.toString(port1.getId()));
            properties.setProperty("className", port1.getClassName());

            MiniAppPhysicalPath physicalPath = new MiniAppPhysicalPath(properties);
            Button port1Btn = new Button(port1.getName());
            port1Btn.addStyleNames("v-button-link", "button-in-cell");
            port1Btn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                Component launchEmbedded = physicalPath.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                UI.getCurrent().addWindow(formWindow);
            });
            //values
            grdADM.addComponent(createCell(card1 != null ? card1.getName() : "", false, false, true, false), 0, 2);
            grdADM.addComponent(createCell(port1.getName(), false, false, true, false), 0, 4);

            if(mmr != null && !mmr.isEmpty()){
                grdADM.addComponent(createCell("MMR", true, false, true, false), 0, 5);
                grdADM.addComponent(createCell(!mmr.isEmpty() ? mmr : "", false, false, true, false), 0, 6);
            }
            if(rmmr != null && !rmmr.isEmpty()){
                grdADM.addComponent(createCell("RMMR", true, false, true, false), 0, 7);
                grdADM.addComponent(createCell(!rmmr.isEmpty() ? rmmr : "", false, false, false, false), 0, 8);
            }
        }
        if(port2 != null){
            mmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "meetmeroom", ipAddress, sessionId);
            rmmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "remotemeetmeroom", ipAddress, sessionId);
            
            //Column 2
            grdADM.addComponent(createCell("CARD 2", true, true, false, false), 1, 1);
            grdADM.addComponent(createCell("PORT 2", true, false, false, false), 1, 3);
            //values
            properties.setProperty("id", Long.toString(port2.getId()));
            properties.setProperty("className", port2.getClassName());

            MiniAppPhysicalPath physicalPath2 = new MiniAppPhysicalPath(properties);
            Button port2Btn = new Button(port2.getName());
            port2Btn.addStyleNames("v-button-link", "button-in-cell");
            port2Btn.addClickListener(event -> {
                Window formWindow = new Window(" ");
                Component launchEmbedded = physicalPath2.launchEmbedded();
                formWindow.setContent(launchEmbedded);
                formWindow.center();
                UI.getCurrent().addWindow(formWindow);
            });
            grdADM.addComponent(createCell(card2 != null ? card2.getName() : "", false, false, false, false), 1, 2);
            grdADM.addComponent(createCell(port2.getName(), false, false, false, false), 1, 4);

            if(rmmr2 != null && !rmmr2.isEmpty()){
                grdADM.addComponent(createCell("RMMR", true, false, true, false), 1, 5);
                grdADM.addComponent(createCell(!rmmr2.isEmpty() ? rmmr2 : "", false, false, true, false), 1, 6);

                if(mmr2 != null && !mmr2.isEmpty()){
                    grdADM.addComponent(createCell(mmr2, false, false, false, false), 1, 8);
                    grdADM.addComponent(createCell("MMR", true, false, false, false), 1, 7);
                }
            }
            //the right column has values, but left are empty
            else if(mmr2 != null && !mmr2.isEmpty()){
                grdADM.addComponent(createCell(" ", true, false, true, false), 0, 8);
                grdADM.addComponent(createCell(" ", false, false, true, false), 0, 7);

                grdADM.addComponent(createCell(mmr2, false, false, false, false), 1, 8);
                grdADM.addComponent(createCell("MMR", true, false, false, false), 1, 7);
            }
        }
          
        grdADM.addComponent(createCell(" ", false, false, false, false), 0, 9, 1, 9);
        String hoster = getHoster(obj);
        if(hoster != null && !hoster.isEmpty()){
            grdADM.addComponent(createCell("DEVICE HOSTER", true, false, true, false), 0, 10);
            grdADM.addComponent(createCell(hoster, false, false, false, false), 1, 10);
        }
        String owner = getOwner(obj);
        if(owner != null && !owner.isEmpty()){
            grdADM.addComponent(createCell("DEVICE OWNER", true, false, true, false), 0, 11);
            grdADM.addComponent(createCell(owner, false, false, false, false), 1, 11);
        }
        String he = getHandE(obj);
        if(he != null && !he.isEmpty()){
            grdADM.addComponent(createCell("DEVICE H&E", true, false, true, false), 0, 12);
            grdADM.addComponent(createCell(he, false, false, false, false), 1, 12);
        }
        
        grdADM.addComponent(createCell("DEVICE LOCATION", true, false, false, false), 0, 14, 1, 14);
        grdADM.addComponent(createCell(getLocation(objLight), false, false, false, false), 0, 15, 1, 15);
        
        grdADM.addComponent(createCell(" ", false, false, false, false), 0, 16, 1, 16);
        if(rackPosition != null && isNumeric(rackPosition) && Integer.valueOf(rackPosition) > 0){
            grdADM.addComponent(createCell("RACK POSITION", true, false, true, false), 0, 17);
            grdADM.addComponent(createCell(rackPosition, false, false, false, false), 1, 17);
        }
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0){
            grdADM.addComponent(createCell("RACK UNITS", true, false, true, false), 0, 18);
            grdADM.addComponent(createCell(rackUnits, false, false, false, false), 1, 18);
        }
        grdADM.addComponent(createIcon(objLight.getClassName()), 0, 19, 1, 19);
        return grdADM;
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
        
        GridLayout grdODF = new GridLayout(2, 9);
        grdODF.addStyleName("report-forms-box");
        grdODF.addComponent(createTitle(objLight.getName(), ODF), 0, 0, 1, 0);
        grdODF.addComponent(createCell("ODF-PORT", true, true, true, false), 0, 1);
        grdODF.addComponent(createCell(port.getName(), false, true, false, false), 1, 1);
        grdODF.addComponent(createCell(" ", false, false, false, false), 0, 2, 1, 2);
        if(rackPostion != null && isNumeric(rackPostion) && Integer.valueOf(rackPostion) > 0){
            grdODF.addComponent(createCell("RACK POSTION", true, false, true, false), 0, 3);
            grdODF.addComponent(createCell(rackPostion, false, false, false, false), 1, 3);
        }
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0){
            grdODF.addComponent(createCell("RACK UNITS", true, false, true, false), 0, 4);
            grdODF.addComponent(createCell(rackUnits, false, false, false, false), 1, 4);
        }
        grdODF.addComponent(createCell(" ", false, false, false, false), 0, 5, 1, 5);
        grdODF.addComponent(createCell("DEVICE LOCATION", true, false, false, false), 0, 6, 1, 6);
        grdODF.addComponent(createCell(getLocation(objLight), false, false, false, false), 0, 7, 1, 7);
        grdODF.addComponent(createIcon(objLight.getClassName()), 0, 8, 1, 8);
        return grdODF;
    }
    
    /**
     * Creates a table for a provider
     * @param providerName the provider name
     * @param providerId the provider id
     * @param legalOwner the legal owner
     * @return a grid layout with the provider's information
     */
    public Component createProviderTable(String providerName, String providerId, String legalOwner){
        
        GridLayout grdProvider = new GridLayout(2, 4);
        grdProvider.addComponent(createTitle(providerName, PROVIDER), 0, 0, 1, 0);
        grdProvider.addStyleName("report-forms-box");
        //Titles
        grdProvider.addComponent(createCell("PROVIDER ID", true, true, true, false), 0, 1);
        grdProvider.addComponent(createCell("LEGAL OWNER", true, false, true, false), 0, 2);
        //values
        if(!legalOwner.isEmpty()){
            grdProvider.addComponent(createCell(providerId, false, true, false, false), 1, 1);
            grdProvider.addComponent(createCell(legalOwner, false, false, false, false), 1, 2);

            grdProvider.addComponent(createIcon(providerName.toLowerCase()), 0, 3, 1, 3);
        }
        return grdProvider;
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
                            
        GridLayout grdProviderSubmarineCable = new GridLayout(2, 10);
        grdProviderSubmarineCable.addStyleName("report-forms-box");
        grdProviderSubmarineCable.addComponent(createTitle(hop1Name, PROVIDER), 0, 0, 1, 0);

        grdProviderSubmarineCable.addComponent(createCell(" ", false, false, false, false), 0, 4, 1, 4);
        if(hop1LegalOwner != null && !hop1LegalOwner.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("LEGAL OWNER", true, true, true, false), 0, 1);
            grdProviderSubmarineCable.addComponent(createCell(hop1LegalOwner, false, true, false, false), 1, 1);
        }
        if(hop1Id != null && !hop1Id.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("PROVIDER ID", true, false, true, false), 0, 2);
            grdProviderSubmarineCable.addComponent(createCell(hop1Id, false, false, false, false), 1, 2);
        }
        if(carfNumber != null && !carfNumber.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("CARF NUMBER", true, false, true, false), 0, 3);
            grdProviderSubmarineCable.addComponent(createCell(carfNumber, false, false, false, false), 1, 3);
        }
        if(euNode != null){
            grdProviderSubmarineCable.addComponent(createCell("EUROPEAN NODE", true, false, true, false), 0, 5);
            grdProviderSubmarineCable.addComponent(createCell(euNode, false, false, false, false), 1, 5);
        }
        if(endNode != null){
            grdProviderSubmarineCable.addComponent(createCell("LANDING NODE", true, false, true, false), 0, 6);
            grdProviderSubmarineCable.addComponent(createCell(endNode, false, false, false, false), 1, 6);
        }
        if(segment != null && !segment.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("SEGMENT", true, false, true, false), 0, 7);
            grdProviderSubmarineCable.addComponent(createCell(segment, false, false, false, false), 1, 7);
        }
        if(moreInformation != null && !moreInformation.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("MORE INFO", true, false, true, false), 0, 8);
            grdProviderSubmarineCable.addComponent(createCell(moreInformation, false, false, false, false), 1, 8);
        }
        grdProviderSubmarineCable.addComponent(createIcon(hop1Name.toLowerCase()), 0, 9, 1, 9);
        
        return grdProviderSubmarineCable;
    }
    
    public Component createVC(RemoteObjectLight vcMplsLink) throws ServerSideException{
        RemoteObjectLight sideA = wsBean.getSpecialAttribute(vcMplsLink.getClassName(), vcMplsLink.getId(), "mplsEndpointA", ipAddress, sessionId).get(0);
        RemoteObjectLight sideB = wsBean.getSpecialAttribute(vcMplsLink.getClassName(), vcMplsLink.getId(), "mplsEndpointB", ipAddress, sessionId).get(0);
        if(sideA != null && sideB != null)
            return createVC(vcMplsLink, sideA, sideB);
        else 
            throw new ServerSideException("Could not determine the end point of the MPLS Link");
    }
    /**
     * Creates a table for a VC (MPLSLinks)
     * @param vcMPLSLink the given object in this case a MPLSLink
     * @param sideA virtual port side A
     * @param sideB virtual port side B
     * @return a grid layout with the vc's information
     * @throws org.kuwaiba.exceptions.ServerSideException could not find the attribute
     */
    public Component createVC(RemoteObjectLight vcMPLSLink, RemoteObjectLight sideA, RemoteObjectLight sideB) throws ServerSideException{
        GridLayout grdVC = new GridLayout(4, 3);
        grdVC.addStyleName("report-forms-box");
        grdVC.addComponent(createTitle(vcMPLSLink.getName(), VC), 0, 0, 3, 0);
        
        String ipSource = wsBean.getAttributeValueAsString(vcMPLSLink.getClassName(), vcMPLSLink.getId(), "ipSource", ipAddress, sessionId);
        if(sideA != null){
            grdVC.addComponent(createShortCell(createCell("PW", true, true, true, false)), 0, 1);
            grdVC.addComponent(createCell(sideA.getName(), false, true, true, false), 1, 1);
            
            if(ipSource != null)
                grdVC.addComponent(createShortCell(createCell("IP", true, false, true, false)), 0, 2);
        }
        else{
            if(ipSource != null && !ipSource.isEmpty()){
                grdVC.addComponent(createShortCell(createCell("IP", true, true, true, false)), 0, 2);   
                grdVC.addComponent(createCell(ipSource, false, false, true, false), 1, 2);
            }
        }
        
        String ipDestiny = wsBean.getAttributeValueAsString(vcMPLSLink.getClassName(), vcMPLSLink.getId(), "ipDestiny", ipAddress, sessionId);
        if(sideB != null){
            grdVC.addComponent(createShortCell(createCell("PW", true, true, true, false)), 2, 1);
            grdVC.addComponent(createCell(sideB.getName(), false, true, false, false), 3, 1);
            if(ipDestiny != null)
                grdVC.addComponent(createShortCell(createCell("IP", true, false, true, false)), 2, 2);
        }
        else{
            if(ipDestiny != null && !ipDestiny.isEmpty()){  
                grdVC.addComponent(createShortCell(createCell("IP", true, true, true, false)), 2, 2);
                grdVC.addComponent(createCell(ipDestiny, false, false, false, false), 3, 2);
            }
        }
        return grdVC;
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
        //Card
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), "GenericBoard", 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        String card = "";
        if(!parents.isEmpty() && parents.get(0).getClassName().toLowerCase().contains("swithc"))
            card = parents.get(parents.size() -1).getName();
        
        String mmr = wsBean.getAttributeValueAsString(port.getClassName(), port.getId(), "meetmeroom", ipAddress, sessionId);
        
        GridLayout grdSwitch = new GridLayout(2, 13);
        grdSwitch.addStyleName("report-forms-box");
        grdSwitch.addComponent(createTitle(objLight.getName(), SWITCH), 0, 0, 1, 0);
       
        Properties properties = new Properties();
        properties.setProperty("id", Long.toString(port.getId()));
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
        grdSwitch.addComponent(createCell("PORT", true, true, true, false), 1, 1);
        grdSwitch.addComponent(createCell(portBtn, false, false, true, false), 1, 2);
        
        if(card != null){
            grdSwitch.addComponent(createCell("CARD", true, true, true, false), 0, 1);
            grdSwitch.addComponent(createCell(card, false, false, false, false), 0, 2);
        }
        
        grdSwitch.addComponent(createCell(" ", false, false, false, false), 0, 3, 1, 3);
       
        String hoster = getHoster(objLight);
        if(hoster != null && !hoster.isEmpty()){
            grdSwitch.addComponent(createCell("DEVICE HOSTER", true, false, true, false), 0, 4);
            grdSwitch.addComponent(createCell(hoster, false, false, false, false), 1, 4);
        }
        String owner = getHoster(objLight);
        if(owner != null && !owner.isEmpty()){
            grdSwitch.addComponent(createCell("DEVICE OWNER", true, false, true, false), 0, 5);
            grdSwitch.addComponent(createCell(owner, false, false, false, false), 1, 5);
        }
        String he = getHandE(objLight);
        if(he != null && !he.isEmpty()){
            grdSwitch.addComponent(createCell("DEVICE H&E", true, false, true, false), 0, 6);
            grdSwitch.addComponent(createCell(he, true, false, false, false), 1, 6);
        }
        
        if(rackPostion != null && isNumeric(rackPostion) && Integer.valueOf(rackPostion) > 0){
            grdSwitch.addComponent(createCell("RACK POSITION", true, false, true, false), 0, 7);
            grdSwitch.addComponent(createCell(rackPostion, false, false, false, false), 1, 7);
        }
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0){
            grdSwitch.addComponent(createCell("RACK UNITS", true, false, true, false), 0, 8);
            grdSwitch.addComponent(createCell(rackUnits, false, false, false, false), 1, 8);
        }
        if(mmr != null && !mmr.isEmpty()){
            grdSwitch.addComponent(createCell("MMR", true, false, true, false), 0, 9);
            grdSwitch.addComponent(createCell(mmr, false, false, false, false), 1, 9);
        }
        
        grdSwitch.addComponent(createCell("DEVICE LOCATION", true, false, true, false), 0, 10, 1, 10);
        grdSwitch.addComponent(createCell(getLocation(objLight), false, false, false, false), 0, 11, 1, 11);
        
        grdSwitch.addComponent(createIcon(objLight.getClassName()), 0, 12, 1, 12);
        return grdSwitch;
    }

    /**
     * Creates a table for an external equipment
     * @param objLight the given object
     * @return a grid layout with the external equipment's information
     * @throws ServerSideException if an attribute need it to create the table could get retrieved 
     */
    public Component createExternalEquipment(RemoteObjectLight objLight) throws ServerSideException{
        
        GridLayout grdExternalEquipment = new GridLayout(2, 5);
        grdExternalEquipment.addStyleName("report-forms-box");
        grdExternalEquipment.addComponent(createTitle(objLight.getName(), EXTERNAL_EQUIPMENT), 0, 0, 1, 0);
        grdExternalEquipment.addComponent(createCell(getLocation(objLight), false, false, false, false), 0, 2, 1, 2);
        String owner = getOwner(objLight);
        if(owner != null && !owner.isEmpty()){
            grdExternalEquipment.addComponent(createCell("DEVICE LOCATION", true, true, false, false), 0, 1, 1, 1);
            grdExternalEquipment.addComponent(createCell("DEVICE OWNER", true, false, true, false), 0, 3);
            grdExternalEquipment.addComponent(createCell(owner, false, false, false, false), 1, 3);
        }
        else
            grdExternalEquipment.addComponent(createExtraWidthCell(createCell("DEVICE LOCATION", true, true, false, false)), 0, 1, 1, 1);
        grdExternalEquipment.addComponent(createIcon(objLight.getClassName()), 0, 4, 1, 4);

        return grdExternalEquipment;
    }
    /**
     * Creates the location of a given object until the City
     * @param objLight the given object
     * @return a string with the location
     * @throws ServerSideException if the parents could no be calculated
     */
    private String getLocation(RemoteObjectLight objLight) throws ServerSideException{
        String location = "";
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(objLight.getClassName(), objLight.getId(), "City",
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        String x = ">";
        for (RemoteObjectLight parent : parents){
            location +=  x + parent.getName() + "<br>";
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
