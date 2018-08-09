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
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.services.persistence.util.Constants;
import org.openide.util.Exceptions;

/**
 * Has methods to create form tables for end to end view
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ServManagerFormCreator{
   /**
     * For ADM side A
     */
    private static final int SIDE_A = 1;
    /**
     * For ADM side B
     */
    private static final int SIDE_B = 2;
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
     * Providers info
     */
    private static final int WACS = 606; 
    private static final int ACE = 609;
    private static final int TATA = 610;
    private static final int ORANGE = 607;
    private static final int PTC = 608;
    private static final int SAT = 612;
    private static final int PCCW = 613;
    private static final int INTER = 614;
    /**
     * Form tables
     */
    private final List<FormStructure> tables;
    
    /**
     * Web service bean reference
     */
    private final WebserviceBean wsBean;
    /**
     * Service reference
     */
    private final RemoteObjectLight service;
    private List<RemoteObjectLight> serviceResources;
    /**
     * IP address reference
     */
    private final String ipAddress;
    /**
     * Session id referecne
     */
    private final String sessionId;
    
    public ServManagerFormCreator(RemoteObjectLight service, WebserviceBean wsBean, String ipAddress, String sessionId) throws ServerSideException {
        this.wsBean = wsBean;
        this.service = service;
        this.ipAddress = ipAddress;
        this.sessionId = sessionId;
        tables = new ArrayList<>();
    }
    
    /**
     * Collects all the data need it to create the form tables
     * @return the final layout
     */
    public Component createForm(){
        try {
            serviceResources = wsBean.getServiceResources(service.getClassName(), service.getId(), ipAddress, sessionId);
            if (serviceResources.isEmpty())
                return new VerticalLayout(new Label(String.format("%s does not have any resources associated to it", service)));
            else {
                for (RemoteObjectLight serviceResource : serviceResources) {
                    FormStructure tempForm = new FormStructure();
                    if (wsBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", ipAddress, sessionId)) {
                        RemoteLogicalConnectionDetails logicalCircuitDetails = wsBean.getLogicalLinkDetails(
                                serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                       
                        //Let's create the nodes corresponding to the endpoint A of the logical circuit
                        List<RemoteObjectLight> parentsUntilFirstComEquipmentA; 
                        if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointA().getClassName(), Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId)){
                            List<RemoteObjectLight> parentsUntilFirstPhysicalPortA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort", ipAddress, sessionId);

                            //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                            if(wsBean.isSubclassOf(parentsUntilFirstPhysicalPortA.get(0).getClassName(), "GenericCommunicationsElement", ipAddress, sessionId))
                                parentsUntilFirstComEquipmentA = Arrays.asList(parentsUntilFirstPhysicalPortA.get(0));
                            else
                                parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortA.get(0).
                                getClassName(), parentsUntilFirstPhysicalPortA.get(0).getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                        }
                        else
                            parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                        RemoteObjectLight aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);
                        Component logicalA = createDeviceTable(aSideEquipmentLogical, 
                                logicalCircuitDetails.getEndpointA());
                        logicalA.setId(Long.toString(aSideEquipmentLogical.getId()));
                        tempForm.setLogicalPartA(logicalA);
                        
                        //now we process the logical link(s)
                        //MPLS
                        if(serviceResource.getClassName().equals("MPLSLink")){
                            ///divCs.add(createVC(serviceResource));
                            Component tempDivC = createVC(serviceResource, logicalCircuitDetails.getEndpointA(), logicalCircuitDetails.getEndpointB());
                            tempForm.getLogicalConnctions().add(tempDivC);
                        }
                        //SDH
                        else{
                            RemoteObject tirbutaryLink = wsBean.getObject(serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                            if(tirbutaryLink != null){
                                String hop2Name = wsBean.getAttributeValueAsString(tirbutaryLink.getClassName(), 
                                        tirbutaryLink.getId(), "hop2Name", ipAddress, sessionId);

                                String legalOwner = wsBean.getAttributeValueAsString(tirbutaryLink.getClassName(),
                                            tirbutaryLink.getId(), "hop2LegalOwner", ipAddress, sessionId);

                                String providerId = tirbutaryLink.getAttribute("hop2Id");
                                if(tirbutaryLink.getAttribute("hop1Name") != null)
                                    tempForm.getLogicalConnctions().add(createProviderTableS(tirbutaryLink));
                                
                                if(hop2Name != null)    
                                    tempForm.getLogicalConnctions().add(createProviderTable(hop2Name, providerId, legalOwner));
                            }
                        }
                        
                        //Now the other side of the logical circuit
                        List<RemoteObjectLight> parentsUntilFirstComEquipmentB;
                        if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointB().getClassName(), Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId)){
                             List<RemoteObjectLight> parentsUntilFirstPhysicalPortB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericPhysicalPort", ipAddress, sessionId);
                              //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                            if(wsBean.isSubclassOf(parentsUntilFirstPhysicalPortB.get(0).getClassName(), "GenericCommunicationsElement", ipAddress, sessionId))
                                parentsUntilFirstComEquipmentB = Arrays.asList(parentsUntilFirstPhysicalPortB.get(0)); 
                            else 
                                parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortB.get(0).
                                getClassName(), parentsUntilFirstPhysicalPortB.get(0).getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                        }
                        else
                            parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                        RemoteObjectLight bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);
                        Component logicalB = createDeviceTable(bSideEquipmentLogical, 
                                logicalCircuitDetails.getEndpointB());
                        logicalB.setId(Long.toString(bSideEquipmentLogical.getId()));
                        tempForm.setLogicalPartB(logicalB);
                        
                        //Now we render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            int i = 2;
                            if (wsBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointA().get(0).getClassName(), 
                                    Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId))
                                i = 3;

                            for(int index = i; index < logicalCircuitDetails.getPhysicalPathForEndpointA().size(); index += 3){
                                RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointA().get(index);
                                //If the physical equipment is not a subclass of GenericCommunicationsElement, nothing will be shown.
                                RemoteObjectLight aSidePhysicalEquipment = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                        nextPhysicalHop.getId(), "ConfigurationItem", ipAddress, sessionId);

                                if(aSidePhysicalEquipment != null && !aSidePhysicalEquipment.getClassName().equals("ODF"))
                                    aSidePhysicalEquipment = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                            nextPhysicalHop.getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                                if(aSidePhysicalEquipment != null){
                                    if(aSidePhysicalEquipment.getClassName().equals("ODF"))
                                        tempForm.setOdfsA(createODF(aSidePhysicalEquipment, nextPhysicalHop));
                                    else
                                        tempForm.setPhysicalPartA(createDeviceTable(aSidePhysicalEquipment, nextPhysicalHop));
                                }
                            }
                        }
                        //Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            int i = 2;
                            if (wsBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointB().get(0).getClassName(), 
                                    Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId))
                                i = 3;
                            for(int index = i; index < logicalCircuitDetails.getPhysicalPathForEndpointB().size(); index += 3){
                                RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointB().get(index);
                                RemoteObjectLight bSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                        nextPhysicalHop.getId(), "ConfigurationItem", ipAddress, sessionId);
                                
                                if(bSideEquipmentPhysical != null && !bSideEquipmentPhysical.getClassName().equals("ODF"))
                                    bSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                            nextPhysicalHop.getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                                 
                                if(bSideEquipmentPhysical != null){
                                    if(bSideEquipmentPhysical.getClassName().equals("ODF"))
                                        tempForm.setOdfsB(createODF(bSideEquipmentPhysical, nextPhysicalHop));
                                    else
                                        tempForm.setPhysicalPartB(createDeviceTable(bSideEquipmentPhysical, nextPhysicalHop));
                                }
                            }
                        }
                    }
                    tables.add(tempForm);
                }//end for
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        return createContent();
    }
   
    /**
     * Check the content of every need it table an creates the content
     * @return a layout with a header, the form tables and a footer
     */
    private Component createContent() {
        VerticalLayout content = new VerticalLayout();
        try {        
            //We create the fom title and the state
            Label lblTitle = new Label(service.getName());
            lblTitle.setId("title");
            RemoteObject obj = wsBean.getObject(service.getClassName(), service.getId(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            //We get the service attributes
            String status = wsBean.getAttributeValueAsString(service.getClassName(), service.getId(), "Status",
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); 
            
            String bandwidth = obj.getAttribute("Bandwidth"); 
            Label lblServStatus = new Label(String.format("Status: %s - Bandwidth: %s" , status != null ? status : " ", bandwidth != null ? bandwidth : " "));
            lblServStatus.setId("properties");
            //we set the form header
            VerticalLayout lytHeader = new VerticalLayout(lblTitle, lblServStatus);
            lytHeader.setId("header");
            content.addComponent(lytHeader);
            HorizontalLayout lytContent = new HorizontalLayout();
            //We add the tables
            lytContent.setSpacing(true);
            lytContent.setId("content");
            boolean isPhysicalSideASet = false, isPhysicalSideBSet = false;
            boolean isLogicalSideASet = false, isLogicalSideBSet = false;
            boolean isMPLSLinkSet;
            boolean isODFASet = false, isODFBSet = false;
            List<String> addedDevices = new ArrayList<>();
            if(!tables.isEmpty()){
                for(FormStructure table : tables) {
                    isMPLSLinkSet = false;
                    if(table.getPhysicalPartA()!= null && !isPhysicalSideASet){
                        lytContent.addComponent(table.getPhysicalPartA());
                        isPhysicalSideASet = true;
                    }
                    
                    if(table.getOdfsA() != null && !isODFASet){
                        lytContent.addComponent(table.getOdfsA());
                        isODFASet = true;
                    }
                    
                    //If the side B is already added so we need de side A we must add the MPLS Link
                    if(addedDevices.contains(table.getLogicalPartB().getId())){
                        table.getLogicalConnctions().forEach(linkTable -> { lytContent.addComponent(linkTable); });
                        isMPLSLinkSet = true;
                    }
                   
                    if(!addedDevices.contains(table.getLogicalPartA().getId())){
                        lytContent.addComponent(table.getLogicalPartA());
                        addedDevices.add(table.getLogicalPartA().getId());
                        isLogicalSideASet = true;
                    }
                    
                    if(!isMPLSLinkSet)
                        table.getLogicalConnctions().forEach(linkTable -> { lytContent.addComponent(linkTable); });
                    
                    if(!addedDevices.contains(table.getLogicalPartB().getId())){
                        lytContent.addComponent(table.getLogicalPartB());
                        addedDevices.add(table.getLogicalPartB().getId());
                        isLogicalSideBSet = true;
                    }
                    
                    //PHYSICAL check here the odf order
                    if(table.getOdfsB() != null && !isODFBSet && (isLogicalSideASet || isLogicalSideBSet)){
                        lytContent.addComponent(table.getOdfsB());
                        isODFBSet = true;
                    }
                    
//                    else if(table.getOdfsA() != null && !isODFASet && (isLogicalSideASet || isLogicalSideBSet)){
//                        lytContent.addComponent(table.getOdfsA());
//                        isODFASet = true;
//                    }
                    
                    if(table.getPhysicalPartB()!= null && !isPhysicalSideBSet){
                        lytContent.addComponent(table.getPhysicalPartB());
                        isPhysicalSideBSet = true;
                    }
                    
                    else if(table.getPhysicalPartA()!= null && isPhysicalSideASet){
                        lytContent.addComponent(table.getPhysicalPartA());
                        isPhysicalSideASet = true;
                    }
                }
            }
            
            content.addComponent(lytContent);
            content.setId("container");
            //We create the foot
            HorizontalLayout lytFoot = new HorizontalLayout(new Label("This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a>", ContentMode.HTML));
            lytFoot.addStyleName("foot");
            content.addComponent(lytFoot);
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        return content;
    }
    
    /**
     * Check which table should be draw according to the device className
     * @param equipment the equipment
     * @param port endPoint
     * @param physicalPath the physical path of the end point 
     * @param side if is side b or a
     * @throws ServerSideException 
     */
    private Component createDeviceTable(RemoteObjectLight equipment, 
            RemoteObjectLight port) throws ServerSideException
    {
        if(wsBean.isSubclassOf(equipment.getClassName(), "GenericDataLinkElement", ipAddress, sessionId))
            return createADM(equipment, port);
        else if (wsBean.isSubclassOf(equipment.getClassName(), "ExternalEquipment", ipAddress, sessionId))
            return createExternalEquipment(equipment);
        else if (wsBean.isSubclassOf(equipment.getClassName(), "Cloud", ipAddress, sessionId))
            return createPeering(equipment);
        else if (wsBean.isSubclassOf(equipment.getClassName(), "GenericNetworkElement", ipAddress, sessionId))
            return createRouter(equipment, port);
        
        return null;
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
     * Creates a cell for the tables, because the cell in grid layout 
     * should be formating individually
     * @param value value to put in the cell
     * @return a formating layout ton insert in the grid layout cell
     */
    private Component createCell(String value){
        HorizontalLayout lytCell = new HorizontalLayout();
        lytCell.addStyleName("cell-with-border");
        lytCell.addComponent(new Label(value, ContentMode.HTML));
        return lytCell;
    }
    
    /**
     * Retrieves the path to the need it icon for the table creation
     * @param icon which icon should be load
     * @return a string with the path to the img
     */
    private Component createIcon(int icon){
        String path;
        switch(icon) {
            case ROUTER: //Router
            path = "/icons/router.png"; break;
            case ADM: //ADM
            path = "/icons/sdhmux.png"; break;
            case ODF: //ODF
            path = "/icons/odf.png"; break;
            case PEERING: //cloud
            path = "/icons/cloud.png"; break;
            case EXTERNAL_EQUIPMENT: //External equipment
            path = "/icons/externalequipment.png"; break;
            case WACS: //WACS
            path = "/icons/logo_wacs.png"; break;
            case ORANGE: //Orange
            path = "/icons/logo_orange.png"; break;
            case PTC: //PTC
            path = "/icons/logo_ptc.png"; break;
            case ACE: //ACE
            path = "/icons/logo_ace.png"; break;
            case TATA: //TATA
            path = "/icons/logo_tata.png"; break;
            case SAT: //3SAT
            path = "/icons/logo_3sat.png"; break;
            case PCCW: //PCCW
            path = "/icons/logo_pccw.png"; break;
            case INTER: //INTERROUTE
            path = "/icons/logo_interoute.png"; break;
            default:
            path = "/icons/no.png"; break;
        }
        Image image = new Image("", new ExternalResource(path));
        image.setWidth("100px");
        HorizontalLayout lytCell = new HorizontalLayout();
        lytCell.addStyleName("cell-with-border-img");
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
        GridLayout grdRouter = new GridLayout(2, 16);
        grdRouter.addComponent(createTitle(objLight.getName(), ROUTER), 0, 0, 1, 0);
        
        grdRouter.addComponent(createCell("CARD"), 0, 1);
        grdRouter.addComponent(createCell("PORT"), 1, 1);
        
        grdRouter.addComponent(createCell(card), 0, 2);
        grdRouter.addComponent(createCell(port.getName()), 1, 2);
        
        grdRouter.addComponent(createCell(" "), 0, 3, 1, 3);
        grdRouter.addComponent(createCell("DEVICE LOCATION"), 0, 4);
        grdRouter.addComponent(createCell(getCityLocation(objLight)), 1, 4);
        grdRouter.addComponent(createCell("DEVICE HOSTER"), 0, 5);
        grdRouter.addComponent(createCell(getHoster(networkDevice)), 1, 5);
        grdRouter.addComponent(createCell("DEVICE OWNER"), 0, 6);
        grdRouter.addComponent(createCell(getOwner(networkDevice)), 1, 6);
        grdRouter.addComponent(createCell("DEVICE H&E"), 0, 7);
        grdRouter.addComponent(createCell(getHandE(networkDevice)), 1, 7);
        
        grdRouter.addComponent(createCell(" "), 0, 8, 1, 8);
        
        grdRouter.addComponent(createCell("RACK POSITION"), 0, 9);
        grdRouter.addComponent(createCell(rackPosition), 1, 9);
        grdRouter.addComponent(createCell("RACK UNITS"), 0, 10);
        grdRouter.addComponent(createCell(rackUnits), 1, 10);
        if(mmr != null && !mmr.isEmpty()){
            grdRouter.addComponent(createCell("MMR"), 0, 11);
            grdRouter.addComponent(createCell(mmr), 1, 11);
        }
        if(rmmr != null && !rmmr.isEmpty()){
            grdRouter.addComponent(createCell("RMMR"), 0, 12);
            grdRouter.addComponent(createCell(rmmr), 1, 12);
        }
        if(moreInformation != null && !moreInformation.isEmpty()){
            grdRouter.addComponent(createCell("MORE INFO"), 0, 13, 1, 13);
            grdRouter.addComponent(createCell(moreInformation), 0, 14, 1, 14);
        }
        grdRouter.addComponent(createIcon(ROUTER), 0, 15, 1, 15);
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
        grdPeering.addComponent(createTitle(objLight.getName(), PEERING), 0, 0, 1, 0);
        grdPeering.addComponent(createCell("IP PEERING"), 0, 1);
        grdPeering.addComponent(createCell(peeringIp != null ? peeringIp : " "), 1, 1);
        grdPeering.addComponent(createCell(" "), 0, 2, 1, 2);
        grdPeering.addComponent(createCell("CIRCUIT ID"), 0, 3);     
        grdPeering.addComponent(createCell(circuitID != null ? circuitID : " "), 1, 3);        
        grdPeering.addComponent(createCell("INTERNAL ID"), 0, 4);
        grdPeering.addComponent(createCell(providerCircuitID != null ? providerCircuitID : " "), 1, 4);
        grdPeering.addComponent(createCell("ASN NUMBER"), 0, 5);
        grdPeering.addComponent(createCell(providerASN != null ? providerASN : " "), 1, 5);
        
        grdPeering.addComponent(createIcon(PEERING), 0, 6, 1, 6);
        return grdPeering;
    }
    
    /**
     * Creates a table for an ADM
     * @param objLight the given object
     * @param port the port where the link ends
     * @return a grid layout with the ADM's information
     * @throws ServerSideException if one attribute need it to create the table coulnd't be retrieved 
     */
    public Component createADM(RemoteObjectLight objLight, 
            RemoteObjectLight port) throws ServerSideException{
        RemoteObject obj = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        String rackUnits = obj.getAttribute("rackUnits");
        String rackPosition = obj.getAttribute("position");
        
        RemoteObjectLight card1 = null, card2 = null, port1 =null, port2 = null;
        
        RemoteObjectSpecialRelationships specialAttributes = wsBean.getSpecialAttributes(port.getClassName(), port.getId(), ipAddress, sessionId);
        List<String> relationships = specialAttributes.getRelationships();
        List<RemoteObjectLightList> relatedObjects = specialAttributes.getRelatedObjects();
        for(int i=0; i<relationships.size(); i++){
            if(relationships.get(i).equals("endpointA") || relationships.get(i).equals("endpointB")){
                RemoteObjectLight link = relatedObjects.get(i).getList().get(0);
                if(relationships.get(i).equals("endpointA")){ //side A
                    port2 = port;
                    card2 = wsBean.getParentsUntilFirstOfClass(port.getClassName(), 
                            port.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                    
                    port1 = wsBean.getSpecialAttribute(link.getClassName(), link.getId(), "endpointB", ipAddress, sessionId).get(0);
                    card1 = wsBean.getParentsUntilFirstOfClass(port1.getClassName(), port1.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                }
            
                else if(relationships.get(i).equals("endpointB")){
                    port1 = port;
                    card1 = wsBean.getParentsUntilFirstOfClass(port1.getClassName(), port1.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                
                    port2 = wsBean.getSpecialAttribute(link.getClassName(), link.getId(), "endpointA", ipAddress, sessionId).get(0);
                    card2 = wsBean.getParentsUntilFirstOfClass(port2.getClassName(), 
                            port2.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                }
            }
        }
        
        String portSpeed1 = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "Speed_port", ipAddress, sessionId);
        String portSpeed2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "Speed_port", ipAddress, sessionId);
        String mmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "meetmeroom", ipAddress, sessionId);
        String rmmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "remotemeetmeroom", ipAddress, sessionId);
        String mmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "meetmeroom", ipAddress, sessionId);
        String rmmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "remotemeetmeroom", ipAddress, sessionId);
        
        GridLayout grdADM = new GridLayout(2, 20);
        
        grdADM.addComponent(createTitle(objLight.getName(), ADM), 0, 0, 1, 0);
        
        grdADM.addComponent(createCell("CARD 1"), 0, 1);
        grdADM.addComponent(createCell("PORT 1"), 0, 3);
        
        //values
        grdADM.addComponent(createCell(card1 != null ? card1.getName() : ""), 0, 2);
        grdADM.addComponent(createCell(port1.getName()), 0, 4);
        if(portSpeed1 != null && !portSpeed1.isEmpty()){
            grdADM.addComponent(createCell("SPEED 1"), 0, 5);
            grdADM.addComponent(createCell(portSpeed1), 0, 6);
        }
        if(mmr != null && !mmr.isEmpty()){
            grdADM.addComponent(createCell("MMR"), 0, 7);
            grdADM.addComponent(createCell(!mmr.isEmpty() ? mmr : ""), 0, 8);
        }
        if(rmmr != null && !rmmr.isEmpty()){
            grdADM.addComponent(createCell("RMMR"), 0, 9);
            grdADM.addComponent(createCell(!rmmr.isEmpty() ? rmmr : ""), 0, 10);
        }
        grdADM.addComponent(createCell("CARD 2"), 1, 1);
        grdADM.addComponent(createCell("PORT 2"), 1, 3);
        //values
        grdADM.addComponent(createCell(card2 != null ? card2.getName() : ""), 1, 2);
        grdADM.addComponent(createCell(port2.getName()), 1, 4);
        if(portSpeed2 != null && !portSpeed2.isEmpty()){
            grdADM.addComponent(createCell(!portSpeed2.isEmpty() ? portSpeed2 : ""), 1, 6);
            grdADM.addComponent(createCell("SPEED 2"), 1, 5);
        }
        if(mmr2 != null && !mmr2.isEmpty()){
            grdADM.addComponent(createCell(mmr2), 1, 8);
            grdADM.addComponent(createCell("MMR"), 1, 7);
        }
        if(rmmr2 != null && !rmmr2.isEmpty()){
            grdADM.addComponent(createCell("RMMR"), 1, 9);
            grdADM.addComponent(createCell(!rmmr2.isEmpty() ? rmmr2 : ""), 1, 10);
        }
        grdADM.addComponent(createCell(" "), 0, 11, 1, 11);
        
        grdADM.addComponent(createCell("DEVICE LOCATION"), 0, 12);
        grdADM.addComponent(createCell(getCityLocation(objLight)), 1, 12);
        grdADM.addComponent(createCell("DEVICE HOSTER"), 0, 13);
        grdADM.addComponent(createCell(getHoster(obj)), 1, 13);
        grdADM.addComponent(createCell("DEVICE OWNER"), 0, 14);
        grdADM.addComponent(createCell(getOwner(obj)), 1, 14);
        grdADM.addComponent(createCell("DEVICE H&E"), 0, 15);
        grdADM.addComponent(createCell(getHandE(obj)), 1, 15);
        
        grdADM.addComponent(createCell(" "), 0, 16, 1, 16);
        
        grdADM.addComponent(createCell("RACK POSITION"), 0, 17);
        grdADM.addComponent(createCell(rackPosition), 1, 17);
        grdADM.addComponent(createCell("RACK UNITS"), 0, 18);
        grdADM.addComponent(createCell(rackUnits), 1, 18);
        
        grdADM.addComponent(createIcon(ADM), 0, 19, 1, 19);
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
        
        GridLayout grdODF = new GridLayout(2, 8);
        grdODF.addComponent(createTitle(objLight.getName(), ODF), 0, 0, 1, 0);
        grdODF.addComponent(createCell("ODF-PORT"), 0, 1);
        grdODF.addComponent(createCell(port.getName()), 1, 1);
        grdODF.addComponent(createCell(" "), 0, 2, 1, 2);
        grdODF.addComponent(createCell("RACK POSTION"), 0, 3);
        grdODF.addComponent(createCell(rackPostion != null ? rackPostion : "Not Set"), 1, 3);
        grdODF.addComponent(createCell("RACK UNITS"), 0, 4);
        grdODF.addComponent(createCell(rackUnits != null ? rackUnits : "Not Set"), 1, 4);
        grdODF.addComponent(createCell(" "), 0, 5, 1, 5);
        grdODF.addComponent(createCell("DEVICE LOCATION"), 0, 6);
        grdODF.addComponent(createCell(getCityLocation(objLight)), 1, 6);
        grdODF.addComponent(createIcon(ODF), 0, 7, 1, 7);
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
        //Titles
        grdProvider.addComponent(createCell("PROVIDER ID"), 0, 1);
        grdProvider.addComponent(createCell("LEGAL OWNER"), 0, 2);
        //values
        grdProvider.addComponent(createCell(providerId), 1, 1);
        grdProvider.addComponent(createCell(legalOwner), 1, 2);

        grdProvider.addComponent(createIcon(selectLogo(providerName)), 0, 3, 1, 3);
        
        return grdProvider;
    }
    
    /**
     * Returns the logo id with the name of the provider
     * @param providerName the provider name
     * @return a in that represents the providers logo id
     */
    public int selectLogo(String providerName){
        if(providerName.toLowerCase().contains("wacs"))
            return WACS;
        else if(providerName.toLowerCase().contains("ace"))
            return ACE;
        else if(providerName.toLowerCase().contains("orange"))
            return ORANGE;       
        else if(providerName.toLowerCase().contains("ptc"))
            return PTC; 
        else if(providerName.toLowerCase().contains("tata"))  
            return TATA;  
        else if(providerName.toLowerCase().contains("sat"))  
            return SAT;  
        else if(providerName.toLowerCase().contains("pccw"))  
            return PCCW;
        else if(providerName.toLowerCase().contains("interoute"))  
            return INTER;             
        else
            return -1;     
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
        
        if(selectLogo(hop1Name) == ACE)
            segment = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "aceSegment", ipAddress, sessionId);
        else if(selectLogo(hop1Name) == WACS)
            segment = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "wacsSegment", ipAddress, sessionId);
            
        String carfNumber = provider.getAttribute("hopCarf"); //listType ProviderType
        String moreInformation = provider.getAttribute("moreInformation");
        String hop1Id = provider.getAttribute("hop1Id");
        
        String hop1LegalOwner = wsBean.getAttributeValueAsString(provider.getClassName(), provider.getId(), "hop1LegalOwner", 
                Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                            
        GridLayout grdProviderSubmarineCable = new GridLayout(2, 10);
        grdProviderSubmarineCable.addComponent(createTitle(hop1Name, PROVIDER), 0, 0, 1, 0);
        
        grdProviderSubmarineCable.addComponent(createCell("LEGAL OWNER"), 0, 1);
        grdProviderSubmarineCable.addComponent(createCell("PROVIDER ID"), 0, 2);
        grdProviderSubmarineCable.addComponent(createCell(" "), 0, 4, 1, 4);
        grdProviderSubmarineCable.addComponent(createCell("EUROPEAN NODE"), 0, 5);
        grdProviderSubmarineCable.addComponent(createCell("LANDING NODE"), 0, 6);
        grdProviderSubmarineCable.addComponent(createCell("SEGMENT"), 0, 7);
        
        grdProviderSubmarineCable.addComponent(createCell(hop1Id), 1, 1);
        grdProviderSubmarineCable.addComponent(createCell(hop1LegalOwner), 1, 2);
        if(carfNumber != null && !carfNumber.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("CARF NUMBER"), 0, 3);
            grdProviderSubmarineCable.addComponent(createCell(carfNumber), 1, 3);
        }
        grdProviderSubmarineCable.addComponent(createCell(euNode), 1, 5);
        grdProviderSubmarineCable.addComponent(createCell(endNode), 1, 6);
        grdProviderSubmarineCable.addComponent(createCell(segment), 1, 7);
        if(moreInformation != null && !moreInformation.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("MORE INFO"), 0, 8);
            grdProviderSubmarineCable.addComponent(createCell(moreInformation), 1, 8);
        }
        grdProviderSubmarineCable.addComponent(createIcon(selectLogo(hop1Name)), 0, 9, 1, 9);
        
        return grdProviderSubmarineCable;
    }
    
    /**
     * Creates a table for a VC (MPLSLinks)
     * @param objLight the given object in this case a MPLSLink
     * @param sideA virtual port side A
     * @param sideB virtual port side B
     * @return a grid layout with the vc's information
     * @throws org.kuwaiba.exceptions.ServerSideException could not find the attribute
     */
    public Component createVC(RemoteObjectLight objLight, RemoteObjectLight sideA, RemoteObjectLight sideB) throws ServerSideException{
        GridLayout grdVC = new GridLayout(2, 3);
        grdVC.addComponent(createTitle(objLight.getName(), VC), 0, 0, 1, 0);
        
        String ipSource = wsBean.getAttributeValueAsString(objLight.getClassName(), objLight.getId(), "ipSource", ipAddress, sessionId);
        grdVC.addComponent(createCell("PW: " + sideA == null ? "" : sideA.toString()), 0, 1);
        grdVC.addComponent(createCell("IP: " + ipSource), 0, 2);

        
        String ipDestiny = wsBean.getAttributeValueAsString(objLight.getClassName(), objLight.getId(), "ipDestiny", ipAddress, sessionId);
        grdVC.addComponent(createCell("PW: " + sideB == null ? "" : sideB.toString()), 1, 1);
        grdVC.addComponent(createCell("IP: " + ipDestiny), 1, 2);
        return grdVC;
    }
    
    /**
     * Creates a table for a Switch
     * @param objLight the given object
     * @return a grid layout with the switch's information
     */
    public Component createSwitch(RemoteObjectLight objLight){
        GridLayout grdSwitch = new GridLayout(2, 12);
        grdSwitch.addComponent(createTitle("SWITCH", SWITCH), 0, 0, 1, 0);
        grdSwitch.addComponent(createCell("CARD"), 0, 1);
        grdSwitch.addComponent(createCell("PORT"), 1, 1);
        
        grdSwitch.addComponent(createCell("CARD XX"), 0, 2);
        grdSwitch.addComponent(createCell("PORT XX"), 1, 2);

        grdSwitch.addComponent(createCell(" "), 0, 3, 1, 3);
          
        grdSwitch.addComponent(createCell("DEVICE LOCATION"), 0, 4);
        grdSwitch.addComponent(createCell("LONDON"), 1, 4);
        grdSwitch.addComponent(createCell("DEVICE HOSTER"), 0, 5);
        grdSwitch.addComponent(createCell("HOSTER"), 1, 5);
        grdSwitch.addComponent(createCell("DEVICE OWNER"), 0, 6);
        grdSwitch.addComponent(createCell("OWNER"), 1, 6);
        grdSwitch.addComponent(createCell("DEVICE H&E"), 0, 7);
        grdSwitch.addComponent(createCell("H&E"), 1, 7);

        grdSwitch.addComponent(createCell("RACK POSITION"), 0, 8);
        grdSwitch.addComponent(createCell("11"), 1, 8);
        grdSwitch.addComponent(createCell("RACK UNITS"), 0, 9);
        grdSwitch.addComponent(createCell("2"), 1, 9);
        grdSwitch.addComponent(createCell("MMR"), 0, 10);
        grdSwitch.addComponent(createCell("xxx"), 1, 10);
        
        grdSwitch.addComponent(createIcon(SWITCH), 0, 11, 1, 11);
        return grdSwitch;
    }

    /**
     * Creates a table for an external equipment
     * @param objLight the given object
     * @return a grid layout with the external equipment's information
     * @throws ServerSideException if an attribute need it to create the table could get retrieved 
     */
    public Component createExternalEquipment(RemoteObjectLight objLight) throws ServerSideException{
        
        GridLayout grdExternalEquipment = new GridLayout(2, 4);
        grdExternalEquipment.addComponent(createTitle(objLight.getName(), EXTERNAL_EQUIPMENT), 0, 0, 1, 0);
        grdExternalEquipment.addComponent(createCell("DEVICE LOCATION"), 0, 1);
        grdExternalEquipment.addComponent(createCell("DEVICE OWNER"), 0, 2);
        grdExternalEquipment.addComponent(createCell(getCityLocation(objLight)), 1, 1);
        grdExternalEquipment.addComponent(createCell(getOwner(objLight)), 1, 2);
        grdExternalEquipment.addComponent(createIcon(EXTERNAL_EQUIPMENT), 0, 3, 1, 3);

        return grdExternalEquipment;
    }
    /**
     * Creates the location of a given object until the City
     * @param objLight the given object
     * @return a string with the location
     * @throws ServerSideException if the parents could no be calculated
     */
    private String createLocation(RemoteObjectLight objLight) throws ServerSideException{
        String location = "";
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(objLight.getClassName(), objLight.getId(), "City",
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        for (RemoteObjectLight parent : parents)
            location += parent.getName() + "<br>";
        
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
     
}
