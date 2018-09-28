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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.services.persistence.util.Constants;
import org.openide.util.Exceptions;

/**
 * Has methods to create form tables for end to end view
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class FormCreator{
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
    private static final int SKYTIC = 615;
    private static final int MOBILE = 617;
    private static final int BICS = 618;
    /**
     * Form tables
     */
    private final LinkedList<FormStructure> tables;
    
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
     * Session id reference
     */
    private final String sessionId;
    /**
     * to keep a track if the view is has Mpls links
     */
    private boolean isMplsView;
    
    public FormCreator(RemoteObjectLight service, WebserviceBean wsBean, String ipAddress, String sessionId) throws ServerSideException {
        this.wsBean = wsBean;
        this.service = service;
        this.ipAddress = ipAddress;
        this.sessionId = sessionId;
        tables = new LinkedList<>();
        isMplsView = false;
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
                    boolean isSideAPeering = false;
                    boolean isSideBPeering = false;
                    if (wsBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", ipAddress, sessionId)) {
                        RemoteLogicalConnectionDetails logicalCircuitDetails = wsBean.getLogicalLinkDetails(
                                serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                        RemoteObjectLight stm = null;
                        //now we process the logical link(s)
                        //MPLS
                        if(serviceResource.getClassName().equals("MPLSLink")){
                            Component tempDivC = createVC(serviceResource, logicalCircuitDetails.getEndpointA(), logicalCircuitDetails.getEndpointB());
                            tempForm.getLogicalConnctions().add(tempDivC);
                            isMplsView = true;
                        }//SDH
                        else{
                            RemoteObject tirbutaryLink = wsBean.getObject(serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                            if(tirbutaryLink != null){
                                String hop2Name = wsBean.getAttributeValueAsString(tirbutaryLink.getClassName(), 
                                        tirbutaryLink.getId(), "hop2Name", ipAddress, sessionId);

                                String legalOwner = wsBean.getAttributeValueAsString(tirbutaryLink.getClassName(),
                                            tirbutaryLink.getId(), "hop2LegalOwner", ipAddress, sessionId);

                                String providerId = tirbutaryLink.getAttribute("hop2Id");
                                if(hop2Name != null)    
                                    tempForm.getLogicalConnctions().add(createProviderTable(hop2Name, providerId, legalOwner));
                                if(tirbutaryLink.getAttribute("hop1Name") != null)
                                    tempForm.getLogicalConnctions().add(createProviderTableS(tirbutaryLink));
                                RemoteObjectLight container = wsBean.getSpecialAttribute(tirbutaryLink.getClassName(), tirbutaryLink.getId(), "sdhDelivers", ipAddress, sessionId).get(0);
                                stm = wsBean.getSpecialAttribute(container.getClassName(), container.getId(), "sdhTransports", ipAddress, sessionId).get(0);
                            }
                        }
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
                        RemoteObjectLight stmEndPointA = null;
                        if(stm != null)
                            stmEndPointA = wsBean.getSpecialAttribute(stm.getClassName(), stm.getId(), "sdhTLEndpointA", ipAddress, sessionId).get(0);
                        Component logicalA = createDeviceTable(aSideEquipmentLogical, 
                                logicalCircuitDetails.getEndpointA(), stmEndPointA);
                        logicalA.setId(Long.toString(aSideEquipmentLogical.getId()));
                        tempForm.setLogicalPartA(logicalA);                        
                        //This only applies if there is a peering, the peering should always be in side B
                        if(aSideEquipmentLogical.getClassName().toLowerCase().contains("cloud"))
                            isSideAPeering = true;
                        
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
                        }else
                            parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                        RemoteObjectLight bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);
                        //We must do this becuase we need the end points of the snmp
                        RemoteObjectLight stmEndPointB = null;
                        if(stm != null)
                            stmEndPointB = wsBean.getSpecialAttribute(stm.getClassName(), stm.getId(), "sdhTLEndpointB", ipAddress, sessionId).get(0);
                        
                        Component logicalB = createDeviceTable(bSideEquipmentLogical, 
                                logicalCircuitDetails.getEndpointB(), stmEndPointB);
                        logicalB.setId(Long.toString(bSideEquipmentLogical.getId()));
                        tempForm.setLogicalPartB(logicalB);
                        //This only applies if there is a peering, the peering should always be in side B
                        if(aSideEquipmentLogical.getClassName().toLowerCase().contains("cloud"))
                            isSideAPeering = true;
                        
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
                                        tempForm.getPhysicalPartA().add(createDeviceTable(aSidePhysicalEquipment, nextPhysicalHop, null));
                                }
                            }
                        }//Now the b side
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
                                        tempForm.getPhysicalPartB().add(createDeviceTable(bSideEquipmentPhysical, nextPhysicalHop, null));
                                }
                            }
                        }
                    }
                    //This is only for peering, we must reorder an set the peering always in side B
                    if(isSideAPeering && !isSideBPeering){
                        
                        Component tempComponent = tempForm.getLogicalPartB();
                        tempForm.setLogicalPartB(tempForm.getLogicalPartA());
                        tempForm.setLogicalPartA(tempComponent);
                        tempComponent = tempForm.getOdfsB();
                        tempForm.setOdfsB(tempForm.getOdfsA());
                        tempForm.setOdfsA(tempComponent);
                        List<Component> listTempComponent = tempForm.getPhysicalPartB();
                        tempForm.setPhysicalPartB(tempForm.getPhysicalPartA());
                        tempForm.setPhysicalPartA(listTempComponent);
                    }
                    
                    tables.addFirst(tempForm);
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
            lblTitle.setId("report-forms-title");
            RemoteObject obj = wsBean.getObject(service.getClassName(), service.getId(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            //We get the service attributes
            String status = wsBean.getAttributeValueAsString(service.getClassName(), service.getId(), "Status",
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); 
            
            String bandwidth = obj.getAttribute("Bandwidth"); 
            Label lblServStatus = new Label(String.format("Status: %s - Bandwidth: %s" , status != null ? status : " ", bandwidth != null ? bandwidth : " "));
            lblServStatus.setId("report-forms-properties");
            //we set the form header
            HorizontalLayout lytHeader = new HorizontalLayout(lblTitle, lblServStatus);
            lytHeader.setId("report-forms-header");
            content.addComponent(lytHeader);
            HorizontalLayout lytContent = new HorizontalLayout();
            //We add the tables
            lytContent.setSpacing(true);
            lytContent.setId("report-forms-content");
            boolean isPhysicalSideASet = false, isPhysicalSideBSet = false;
            boolean isODFASet = false, isODFBSet = false;
            List<String> addedDevices = new ArrayList<>();
            if(!tables.isEmpty()){
                if(tables.size() > 1) 
                    orderTables();
                
                for(FormStructure table : tables) {
                    //We add the side A - physical part
                    if(!table.getPhysicalPartA().isEmpty() && !isPhysicalSideASet){
                        table.getPhysicalPartA().forEach(physicalTable -> {
                            lytContent.addComponent(physicalTable);  
                        });
                        isPhysicalSideASet = true;
                    }
                    //If the view has Mpls links the ODFs should go between the physical and the logical devices
                    if(table.getOdfsA() != null && !isODFASet && isMplsView){
                            lytContent.addComponent(table.getOdfsA());
                            isODFASet = true;
                    }
                    //we add the side A - logical part
                    if(table.getLogicalPartA() != null && 
                            !addedDevices.contains(table.getLogicalPartA().getId()))
                    {
                        lytContent.addComponent(table.getLogicalPartA());
                        addedDevices.add(table.getLogicalPartA().getId());
                        //We add the ODF side A
                        if(table.getOdfsA() != null && !isODFASet && !isMplsView){
                            lytContent.addComponent(table.getOdfsA());
                            isODFASet = true;
                        }
                    } 
                    //L I N K  We add the link tables
                    table.getLogicalConnctions().forEach(linkTable -> { lytContent.addComponent(linkTable); });
                    
                    //If the view has Mpls links the ODFs should go between the physical and the logical devices
                    if(table.getOdfsB() != null && !isODFBSet && isMplsView){
                        lytContent.addComponent(table.getOdfsB());
                        isODFBSet = true;
                    }
                    
                    //we add the logical side B
                    if(table.getLogicalPartB() != null && !addedDevices.contains(table.getLogicalPartB().getId())){
                        //we add the ODF side B
                        if(table.getOdfsB() != null && !isODFBSet && !addedDevices.contains(table.getLogicalPartB().getId()) && !isMplsView){
                            lytContent.addComponent(table.getOdfsB());
                            isODFBSet = true;
                        }
                        lytContent.addComponent(table.getLogicalPartB());
                        addedDevices.add(table.getLogicalPartB().getId());
                        //We add the physical side B
                        if(!table.getPhysicalPartB().isEmpty() && !isPhysicalSideBSet){
                            table.getPhysicalPartB().forEach(physicalTable -> {
                                lytContent.addComponent(physicalTable);
                            });
                            isPhysicalSideBSet = true;
                        }
                    }
                }
            }
            content.addComponent(lytContent);
            content.setId("report-forms-container");
            content.addStyleName("report-forms");
            //We create the foot
            HorizontalLayout lytFoot = new HorizontalLayout(new Label("This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a>", ContentMode.HTML));
            content.addComponent(lytFoot);
            content.setComponentAlignment(lytFoot, Alignment.BOTTOM_CENTER);
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        return content;
    }
    
    private int[] search(long id){
        int[] result = new int[2];
        for(int i=1; i< tables.size(); i++){
            if(tables.get(i).getLogicalPartA().getId().equals(Long.toString(id))){
                result[0] = SIDE_A;
                result[1] = i;
            }
            else if(tables.get(i).getLogicalPartB().getId().equals(Long.toString(id)))
                result[0] = SIDE_B;
                result[1] = i;
        }
        result[0] = 0;
        result[1] = -1;
        return result;
    }
    
    /**
     * Reads the list of tables, (every side) to order 
     */
    private void orderTables(){
        int i = 0;
        boolean isSideAChecked = false, isSideBChecked = false; 
        int[] location = {0, -1};
        
        while(i < tables.size()){
            location[0] = 0; location[1] = -1;
            if(tables.get(i).getLogicalPartA() != null && !isSideAChecked){
                location = search(Long.valueOf(tables.get(i).getLogicalPartA().getId()));
                if(location[0] != 0 && location[1] != -1)
                    moveRouter(SIDE_A, i, location[0], location[1]);
                else if(location[0] == 0 && location[1] == -1)
                    isSideAChecked = true;
            }
            else if(tables.get(i).getLogicalPartA() == null)
                isSideAChecked = true;
            
            if(tables.get(i).getLogicalPartB() != null && isSideAChecked && !isSideBChecked){
                location = search(Long.valueOf(tables.get(i).getLogicalPartB().getId()));
                if(location[0] != 0 && location[1] != -1)
                    moveRouter(SIDE_B, i, location[0], location[1]);
                else if(location[0] == 0 && location[1] == -1)
                    isSideBChecked = true;
            }
            else if(tables.get(i).getLogicalPartB() == null)
                isSideBChecked = true;
            
            if(location[0] == 0 && location[1] == -1 && isSideAChecked && isSideBChecked){
                i++;
                isSideAChecked = false;
                isSideBChecked = false;
            }
        }
    }
    
    /**
     * Reorder the list of tables, removing the repeated routers and 
     * put them in the right place
     * e.g.
     * Table before: [R2-R3][R4-R1][R1-R2] start with R2, (first iteration) i=0 from:A pos: 2 side:B
     * (second iteration)[R1-][R2-R3][R4-R1] i=0 from:A pos:2 side:B
     * (third iteration) [R4-][R1-][R2-R3] from:A pos:-1 side: 0
     * 
     * Same routers, with the same order but with opposite sides.
     * [R2-R3][R4-R1][R2-R1] R2, i=0 from:A pos:2 side:A change
     * [R1-][R2-R3][R4-R1] i=0 from:A pos:2 side:B
     * [R4-][R1-][R2-R3] from:A pos:-1 side:0
     * -----
     * Same routers, with the same order but with opposite sides.
     * [R3-R2][R4-R1][R1-R2] i=0 from:B pos:2 side:B change
     * [R3-R2][-R1][R4-R1] i=1 from:B pos:2 side:B
     * [R3-R2][-R1][-R4]
     *
     * Same routers, with the same order but with opposite sides.
     * [R3-R2][R4-R1][R2-R1] i=0 from:B pos:2 side:A
     * [R3-R2][-R1][R4-R1] i=1 from:B pos:2 side:B
     * [R3-R2][-R1][-R4]
     * @param sourceSide the side of the current evaluated router
     * @param sourceIndex the current evaluated router
     * @param side the side if the router was found as repeat
     * @param index the index if the router was found as repeat
     */
    private void moveRouter(int sourceSide, int sourceIndex, int side, int index){
        if(sourceSide == SIDE_A){
            if(side == SIDE_B){
                tables.get(index).setLogicalPartB(null);
                FormStructure tableToMove = tables.get(index);
                tables.remove(index);
                tables.add(sourceIndex, tableToMove);
            }
            else if(side == SIDE_A){
                tables.get(index).setLogicalPartA(tables.get(index).getLogicalPartB());
                tables.get(index).setLogicalPartB(null);
                FormStructure tableToMove = tables.get(index);
                tables.remove(index);
                tables.add(sourceIndex, tableToMove);
            }
        }
        else if(sourceSide == SIDE_B){
            if(side == SIDE_B){
                tables.get(index).setLogicalPartB(tables.get(index).getLogicalPartA());
                tables.get(index).setLogicalPartA(null);     
                FormStructure tableToMove = tables.get(index);
                tables.remove(index);
                tables.add(tables.size() == sourceIndex ? sourceIndex : sourceIndex + 1, tableToMove);
            }
            else if(side == SIDE_A){
                tables.get(index).setLogicalPartA(null);
                FormStructure tableToMove = tables.get(index);
                tables.remove(index);
                tables.add(tables.size() == sourceIndex ? sourceIndex : sourceIndex + 1, tableToMove);
            }
        }
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
            RemoteObjectLight port, RemoteObjectLight stm) throws ServerSideException
    {
        if(wsBean.isSubclassOf(equipment.getClassName(), "GenericDataLinkElement", ipAddress, sessionId))
            return createADM(equipment, port, stm);
        else if (wsBean.isSubclassOf(equipment.getClassName(), "ExternalEquipment", ipAddress, sessionId))
            return createExternalEquipment(equipment);
        else if (wsBean.isSubclassOf(equipment.getClassName(), "Cloud", ipAddress, sessionId))
            return createPeering(equipment);
        else if (equipment.getClassName().toLowerCase().contains("router"))
            return createRouter(equipment, port);
        else if (equipment.getClassName().toLowerCase().contains("switch"))
            return createSwitch(equipment, port);
        
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
    private Component createCell(String value, boolean bold, boolean topBorder, boolean rightBorder, boolean noBottom){
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
        
        lytCell.addComponent(new Label(value.replace("\n", "<br>"), ContentMode.HTML));
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
            case SKYTIC: //SKY TIC
                path = "/icons/logo_skytic.png"; break;
            case MOBILE: //9 Movile
                path = "/icons/logo_mobile.png"; break;
            case BICS: //9 Movile
                path = "/icons/logo_bics.png"; break;    
            case INTER: //INTERROUTE
                path = "/icons/logo_interoute.png"; break;
            default:
                path = "/icons/no.png"; break;
        }
        Image image = new Image("", new ExternalResource(path));
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
        
        grdRouter.addComponent(createCell(card, false, false, true, false), 0, 2);
        grdRouter.addComponent(createCell(port.getName(), false, false, false, false), 1, 2);
        
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
        grdRouter.addComponent(createIcon(ROUTER), 0, 17, 1, 17);
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
        if(circuitID != null){
            grdPeering.addComponent(createCell("CIRCUIT ID", true, false, true, false), 0, 3);     
            grdPeering.addComponent(createCell(circuitID, false, false, false, false), 1, 3);        
        }
        if(providerCircuitID != null){
            grdPeering.addComponent(createCell("INTERNAL ID", true, false, true, false), 0, 4);
            grdPeering.addComponent(createCell(providerCircuitID, false, false, false, false), 1, 4);
        }
        if(providerASN != null){
            grdPeering.addComponent(createCell("ASN NUMBER", true, false, true, false), 0, 5);
            grdPeering.addComponent(createCell(providerASN, false, false, false, false), 1, 5);
        }
        grdPeering.addComponent(createIcon(PEERING), 0, 6, 1, 6);
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
                            port.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                    
                    port2 = stmEndPoint;
                    card2 = wsBean.getParentsUntilFirstOfClass(port2.getClassName(), port2.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                }
            
                else if(relationships.get(i).equals("endpointB")){
                    port2 = port;
                    card2 = wsBean.getParentsUntilFirstOfClass(port2.getClassName(), port2.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                
                    port1 = stmEndPoint;
                    card1 = wsBean.getParentsUntilFirstOfClass(port1.getClassName(), port1.getId(), "IpBoard", 
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
                }
            }
        }
        
        String mmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "meetmeroom", ipAddress, sessionId);
        String rmmr = wsBean.getAttributeValueAsString(port1.getClassName(), port1.getId(), "remotemeetmeroom", ipAddress, sessionId);
        String mmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "meetmeroom", ipAddress, sessionId);
        String rmmr2 = wsBean.getAttributeValueAsString(port2.getClassName(), port2.getId(), "remotemeetmeroom", ipAddress, sessionId);
        
        GridLayout grdADM = new GridLayout(2, 20);
        grdADM.addStyleName("report-forms-box");
        grdADM.addComponent(createTitle(objLight.getName(), ADM), 0, 0, 1, 0);
        //Column 1
        grdADM.addComponent(createCell("CARD 1", true, true, true, false), 0, 1);
        grdADM.addComponent(createCell("PORT 1", true, false, true, false), 0, 3);
        
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
        //Column 2
        grdADM.addComponent(createCell("CARD 2", true, true, false, false), 1, 1);
        grdADM.addComponent(createCell("PORT 2", true, false, false, false), 1, 3);
        //values
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
            grdADM.addComponent(createCell(getHandE(obj), false, false, false, false), 1, 12);
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
        grdODF.addComponent(createIcon(ODF), 0, 8, 1, 8);
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
        grdProvider.addComponent(createCell(providerId, false, true, false, false), 1, 1);
        grdProvider.addComponent(createCell(legalOwner, false, false, false, false), 1, 2);

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
        else if(providerName.toLowerCase().contains("skytic"))  
            return SKYTIC;   
        else if(providerName.toLowerCase().contains("9mobile"))  
            return MOBILE; 
        else if(providerName.toLowerCase().contains("bics"))  
            return BICS; 
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
        grdProviderSubmarineCable.addStyleName("report-forms-box");
        grdProviderSubmarineCable.addComponent(createTitle(hop1Name, PROVIDER), 0, 0, 1, 0);
        grdProviderSubmarineCable.addComponent(createCell("LEGAL OWNER", true, true, true, false), 0, 1);
        grdProviderSubmarineCable.addComponent(createCell("PROVIDER ID", true, false, true, false), 0, 2);
        grdProviderSubmarineCable.addComponent(createCell(" ", false, false, false, false), 0, 4, 1, 4);
        grdProviderSubmarineCable.addComponent(createCell("EUROPEAN NODE", true, false, true, false), 0, 5);
        grdProviderSubmarineCable.addComponent(createCell("LANDING NODE", true, false, true, false), 0, 6);
        grdProviderSubmarineCable.addComponent(createCell("SEGMENT", true, false, true, false), 0, 7);
        
        grdProviderSubmarineCable.addComponent(createCell(hop1LegalOwner, false, true, false, false), 1, 1);
        grdProviderSubmarineCable.addComponent(createCell(hop1Id, false, false, false, false), 1, 2);
        if(carfNumber != null && !carfNumber.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("CARF NUMBER", true, false, true, false), 0, 3);
            grdProviderSubmarineCable.addComponent(createCell(carfNumber, false, false, false, false), 1, 3);
        }
        grdProviderSubmarineCable.addComponent(createCell(euNode, false, false, false, false), 1, 5);
        grdProviderSubmarineCable.addComponent(createCell(endNode, false, false, false, false), 1, 6);
        grdProviderSubmarineCable.addComponent(createCell(segment, false, false, false, false), 1, 7);
        if(moreInformation != null && !moreInformation.isEmpty()){
            grdProviderSubmarineCable.addComponent(createCell("MORE INFO", true, false, true, false), 0, 8);
            grdProviderSubmarineCable.addComponent(createCell(moreInformation, false, false, false, false), 1, 8);
        }
        grdProviderSubmarineCable.addComponent(createIcon(selectLogo(hop1Name)), 0, 9, 1, 9);
        
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
     * @return a grid layout with the switch's information
     * @throws org.kuwaiba.exceptions.ServerSideException
     */
    public Component createSwitch(RemoteObjectLight objLight, RemoteObjectLight port) throws ServerSideException{
        
         RemoteObject switch_ = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        String rackPostion = switch_.getAttribute("position");
        String rackUnits = switch_.getAttribute("rackUnits");
        
        GridLayout grdSwitch = new GridLayout(2, 12);
        grdSwitch.addStyleName("report-forms-box");
        grdSwitch.addComponent(createTitle(objLight.getName(), SWITCH), 0, 0, 1, 0);
        grdSwitch.addComponent(createCell("CARD", true, true, true, false), 0, 1);
        grdSwitch.addComponent(createCell("PORT", true, false, true, false), 1, 1);
        
        grdSwitch.addComponent(createCell("CARD XX", false, false, false, false), 0, 2);
        grdSwitch.addComponent(createCell("PORT XX", false, false, false, false), 1, 2);

        grdSwitch.addComponent(createCell(" ", false, false, false, false), 0, 3, 1, 3);
          
        grdSwitch.addComponent(createCell("DEVICE LOCATION", true, false, true, false), 0, 4);
        grdSwitch.addComponent(createCell(getCityLocation(objLight), false, false, false, false), 1, 4);
        grdSwitch.addComponent(createCell("DEVICE HOSTER", true, false, true, false), 0, 5);
        grdSwitch.addComponent(createCell(getHoster(objLight), false, false, false, false), 1, 5);
        grdSwitch.addComponent(createCell("DEVICE OWNER", true, false, true, false), 0, 6);
        grdSwitch.addComponent(createCell(getHoster(objLight), false, false, false, false), 1, 6);
        grdSwitch.addComponent(createCell("DEVICE H&E", true, false, true, false), 0, 7);
        grdSwitch.addComponent(createCell(getHandE(objLight), true, false, false, false), 1, 7);
        if(rackPostion != null && isNumeric(rackPostion) && Integer.valueOf(rackPostion) > 0){
            grdSwitch.addComponent(createCell("RACK POSITION", true, false, true, false), 0, 8);
            grdSwitch.addComponent(createCell(rackPostion, false, false, false, false), 1, 8);
        }
        if(rackUnits != null && isNumeric(rackUnits) && Integer.valueOf(rackUnits) > 0){
            grdSwitch.addComponent(createCell("RACK UNITS", true, false, true, false), 0, 9);
            grdSwitch.addComponent(createCell(rackUnits, false, false, false, false), 1, 9);
        }
        grdSwitch.addComponent(createCell("MMR", true, false, true, false), 0, 10);
        grdSwitch.addComponent(createCell("xxx", false, false, false, false), 1, 10);
        
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
        grdExternalEquipment.addComponent(createIcon(EXTERNAL_EQUIPMENT), 0, 4, 1, 4);

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
