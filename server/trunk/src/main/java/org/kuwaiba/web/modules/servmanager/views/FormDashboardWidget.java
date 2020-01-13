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

import com.neotropic.kuwaiba.modules.views.ObjectLinkObjectDefinition;
import com.vaadin.server.Page;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLinkObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.services.persistence.util.Constants;
import org.openide.util.Exceptions;

/**
 * Has methods to create form tables for end to end view
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class FormDashboardWidget extends AbstractDashboardWidget{
    /**
     * For ADM side A
     */
    private static final int SIDE_A = 1;
    /**
     * For ADM side B
     */
    private static final int SIDE_B = 2;
    /**
     * Form tables
     */
    private LinkedList<FormStructure> tables;
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
    
    private TableCreator tableCreator;
    
    public FormDashboardWidget(AbstractDashboard rootComponent, RemoteObjectLight service, WebserviceBean wsBean){
        super("Forms", rootComponent);
        this.wsBean = wsBean;
        this.service = service;
        this.ipAddress = Page.getCurrent().getWebBrowser().getAddress();
        this.sessionId = ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId();
        
        isMplsView = false;
        tableCreator = new TableCreator(service, wsBean);
        createCover();
    }
    
    @Override
    public void createCover() {
        VerticalLayout lytRelationshipsWidgetCover = new VerticalLayout();
        Label lblText = new Label("Forms");
        lblText.setStyleName("text-bottomright");
        lytRelationshipsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
        });
        
        lytRelationshipsWidgetCover.addComponent(lblText);
        lytRelationshipsWidgetCover.setSizeFull();
        lytRelationshipsWidgetCover.setStyleName("dashboard_cover_widget-darkgrey");
        this.coverComponent = lytRelationshipsWidgetCover;
        addComponent(coverComponent);
    }
    
    /**
     * Collects all the data need it to create the form tables
     * @return the final layout
     */
    private void readEndToEndView(){
    //ToDo find a way to draw the tables
        try {
            serviceResources = wsBean.getServiceResources(service.getClassName(), service.getId(), ipAddress, sessionId);
            tables = new LinkedList<>();
            FormStructure tempForm = null;
            if (!serviceResources.isEmpty()) {
                for (RemoteObjectLight serviceResource : serviceResources) {
                     
                    List<Component> listTempODFsA = new ArrayList<>();
                    List<Component> listTempODFsB = new ArrayList<>();
                    
                    
                    List<RemoteObjectLight> listAddedLogicalA = new ArrayList<>();
                    List<RemoteObjectLight> listAddedLogicalB = new ArrayList<>();
                    
                    List<RemoteObjectLight> listAddedPhysicalA = new ArrayList<>();
                    List<RemoteObjectLight> listAddedPhysicalB = new ArrayList<>();
                    boolean isSideAPeering = false;
                    boolean isSideBPeering = false;
                    if (wsBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", ipAddress, sessionId)) {
                        tempForm = new FormStructure();
                        RemoteLogicalConnectionDetails logicalCircuitDetails = wsBean.getLogicalLinkDetails(
                                serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                        RemoteObjectLight stm = null;
                        //now we process the logical link(s)
                        //MPLS
                        if(serviceResource.getClassName().equals("MPLSLink")){
                            Component tempDivC = tableCreator.createVC(serviceResource, logicalCircuitDetails.getEndpointA(), logicalCircuitDetails.getEndpointB());
                            tempForm.getLogicalConnctions().add(tempDivC);
                            isMplsView = true;
                        }//SDH
                        else{
                            RemoteObject tributaryLink = wsBean.getObject(serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                            if(tributaryLink != null){
                                String hop2Name = wsBean.getAttributeValueAsString(tributaryLink.getClassName(), 
                                        tributaryLink.getId(), "hop2Name", ipAddress, sessionId);

                                String legalOwner = wsBean.getAttributeValueAsString(tributaryLink.getClassName(),
                                            tributaryLink.getId(), "hop2LegalOwner", ipAddress, sessionId);

                                String providerId = tributaryLink.getAttribute("hop2Id");
                                if(hop2Name != null)    
                                    tempForm.getLogicalConnctions().add(tableCreator.createProviderTable(hop2Name, providerId, legalOwner));
                                if(tributaryLink.getAttribute("hop1Name") != null)
                                    tempForm.getLogicalConnctions().add(tableCreator.createProviderTableS(tributaryLink));
                                
                                RemoteObjectSpecialRelationships specialAttributes = wsBean.getSpecialAttributes(tributaryLink.getClassName(), tributaryLink.getId(), ipAddress, sessionId);
                                if(specialAttributes.getRelationships().contains("sdhDelivers")){
                                    RemoteObjectLight container = wsBean.getSpecialAttribute(tributaryLink.getClassName(), tributaryLink.getId(), "sdhDelivers", ipAddress, sessionId).get(0);
                                    specialAttributes = wsBean.getSpecialAttributes(container.getClassName(), container.getId(), ipAddress, sessionId);

                                    if(specialAttributes.getRelationships().contains("sdhTransports"))
                                        stm = wsBean.getSpecialAttribute(container.getClassName(), container.getId(), "sdhTransports", ipAddress, sessionId).get(0);
                                    else
                                        Notifications.showWarning(String.format("The resource: %s, is TributaryLink but has no relationship sdhTransport, could be malformed, this form cannot be rendered correctly", 
                                            container));
                                }
                                else
                                    Notifications.showWarning(String.format("The resource: %s, is TributaryLink but has no relationship sdhDelivers, could be malformed, this form cannot be rendered correctly", 
                                        tributaryLink));
                            }
                        }
                        RemoteObjectLight aSideEquipmentLogical =  null;
                        if(logicalCircuitDetails.getEndpointA() != null){                       
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

                            aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);
                            RemoteObjectLight stmEndPointA = null;

                            if(stm != null)
                                stmEndPointA = wsBean.getSpecialAttribute(stm.getClassName(), stm.getId(), "sdhTLEndpointA", ipAddress, sessionId).get(0);
                            Component logicalA = createDeviceTable(aSideEquipmentLogical, 
                                    logicalCircuitDetails.getEndpointA(), stmEndPointA);
                            logicalA.setId(aSideEquipmentLogical.getId());
                            tempForm.setLogicalPartA(logicalA);  
                            listAddedLogicalA.add(aSideEquipmentLogical);
                            //This only applies if there is a peering, the peering should always be in side B
                            if(aSideEquipmentLogical.getClassName().toLowerCase().contains("cloud"))
                                isSideAPeering = true;
                        }
                        //Now the other side of the logical circuit
                        if(logicalCircuitDetails.getEndpointB() != null){
                            List<RemoteObjectLight> parentsUntilFirstComEquipmentB = null;
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
                            logicalB.setId(bSideEquipmentLogical.getId());
                            tempForm.setLogicalPartB(logicalB);
                            listAddedLogicalB.add(bSideEquipmentLogical);
                        }
                        //This only applies if there is a peering, the peering should always be in side B
                        if(aSideEquipmentLogical != null && aSideEquipmentLogical.getClassName().toLowerCase().contains("cloud"))
                            isSideAPeering = true;
                        //Now we render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            List<RemoteObjectLinkObject> physicalPathReaded = physicalPathReader(logicalCircuitDetails.getPhysicalPathForEndpointA());
                            
                            for (RemoteObjectLinkObject obj : physicalPathReaded) {
                                if(!listAddedPhysicalB.contains(obj.getDeviceB()) && !listAddedPhysicalA.contains(obj.getDeviceB()) &&
                                        !listAddedLogicalA.contains(obj.getDeviceB()) && !listAddedLogicalB.contains(obj.getDeviceB())){

                                    listAddedPhysicalA.add(obj.getDeviceB());
                                    if(obj.getDeviceB().getClassName().equals("ODF"))
                                        listTempODFsA.add(createDeviceTable(obj.getDeviceB(), obj.getPhysicalEndpointObjectB(), null));
                                    else
                                        tempForm.getPhysicalPartA().add(createDeviceTable(obj.getDeviceB(), obj.getPhysicalEndpointObjectB(), null));
                                }
                            }
                        }//Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            List<RemoteObjectLinkObject> physicalPathReaded = physicalPathReader(logicalCircuitDetails.getPhysicalPathForEndpointB());
                            
                            for (RemoteObjectLinkObject obj : physicalPathReaded) {
                                if(!listAddedPhysicalB.contains(obj.getDeviceB()) && !listAddedPhysicalA.contains(obj.getDeviceB()) &&
                                        !listAddedLogicalA.contains(obj.getDeviceB()) && !listAddedLogicalB.contains(obj.getDeviceB())){
                                    
                                    
                                    listAddedPhysicalB.add(obj.getDeviceB());
                                    if(obj.getDeviceB().getClassName().equals("ODF"))
                                        listTempODFsB.add(createDeviceTable(obj.getDeviceB(), obj.getPhysicalEndpointObjectB(), null));
                                    else
                                        tempForm.getPhysicalPartB().add(createDeviceTable(obj.getDeviceB(), obj.getPhysicalEndpointObjectB(), null));
                                }
                            }
                        }
                    
                        if(tempForm != null){
                            tempForm.setOdfsA(listTempODFsA);
                            tempForm.setOdfsB(listTempODFsB);
                            //This is only for peering, we must reorder an set the peering always in side B
                            if(isSideAPeering && !isSideBPeering){

                                Component tempComponent = tempForm.getLogicalPartB();
                                tempForm.setLogicalPartB(tempForm.getLogicalPartA());  
                                tempForm.setLogicalPartA(tempComponent);
                                //ODFs
                                List<Component> tempODFs = tempForm.getOdfsB();
                                tempForm.setOdfsB(tempForm.getOdfsA());
                                tempForm.setOdfsA(tempODFs);
                                List<Component> listTempComponent = tempForm.getPhysicalPartB();
                                tempForm.setPhysicalPartB(tempForm.getPhysicalPartA());
                                tempForm.setPhysicalPartA(listTempComponent);
                            }

                            tables.addFirst(tempForm);
                        }
                    }
                }//end for
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
    }
   
    /**
     * Check the content of every need it table an creates the content
     * @return a layout with a header, the form tables and a footer
     */
    private Component createTables(){
        //return new VerticalLayout(new Label(String.format("%s does not have any resources associated to it", service)));
        VerticalLayout content = new VerticalLayout();
        try {        
            //We create the fom title and the state
            Label lblTitle = new Label(service.getName());
            lblTitle.setId("report-forms-title");
            //We get the service attributes
            String status = wsBean.getAttributeValueAsString(service.getClassName(), service.getId(), "Status",
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); 
                       
            String bandwidth = wsBean.getAttributeValueAsString(service.getClassName(), service.getId(), "Bandwidth",
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
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
                    //If the view has MPLSLinks the ODFs should go between the physical and the logical devices
                    if(table.getOdfsA() != null && !isODFASet && isMplsView){
                        for(Component odfComponentA : table.getOdfsA())
                            lytContent.addComponent(odfComponentA);
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
                            for(Component odfComponentA : table.getOdfsA())
                                lytContent.addComponent(odfComponentA);
                            //isODFASet = true;
                        }
                    } 
                    //L I N K  We add the link tables
                    table.getLogicalConnctions().forEach(linkTable -> { lytContent.addComponent(linkTable); });
                    
                    //If the view has Mpls links the ODFs should go between the physical and the logical devices
                    if(table.getOdfsB() != null && !isODFBSet && !isMplsView){
                        for(Component odfComponentB : table.getOdfsB())
                            lytContent.addComponent(odfComponentB);
                        //isODFBSet = true;
                    }
                    //we add the logical side B
                    if(table.getLogicalPartB() != null && !addedDevices.contains(table.getLogicalPartB().getId())){
                        //we add the ODF side B
                        lytContent.addComponent(table.getLogicalPartB());
                        addedDevices.add(table.getLogicalPartB().getId());
                        
                        if(table.getOdfsB() != null && !isODFBSet && isMplsView){
                            for(Component odfComponentB : table.getOdfsB())
                                lytContent.addComponent(odfComponentB);
                            //isODFBSet = true;
                        }
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
    
    @Override
    public void createContent() {
        readEndToEndView();
        Component createTables = createTables();
        this.contentComponent = createTables;
    }
    
    /**
     * search devices inside the list of tables.
     * @param id
     * @return 
     */
    private int[] search(String id){
        int[] result = new int[2];
        for(int i=1; i< tables.size(); i++){
            if(tables.get(i).getLogicalPartA() != null && tables.get(i).getLogicalPartA().getId().equals(id)){
                result[0] = SIDE_A;
                result[1] = i;
            }
            else if(tables.get(i).getLogicalPartB() != null && tables.get(i).getLogicalPartB().getId().equals(id)){
                result[0] = SIDE_B;
                result[1] = i;
            }
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
                location = search(tables.get(i).getLogicalPartA().getId());
                if(location[0] != 0 && location[1] != -1)
                    moveRouter(SIDE_A, i, location[0], location[1]);
                else if(location[0] == 0 && location[1] == -1)
                    isSideAChecked = true;
            }
            else if(tables.get(i).getLogicalPartA() == null)
                isSideAChecked = true;
            
            if(tables.get(i).getLogicalPartB() != null && isSideAChecked && !isSideBChecked){
                location = search(tables.get(i).getLogicalPartB().getId());
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
            return tableCreator.createADM(equipment, port, stm);
        else if (wsBean.isSubclassOf(equipment.getClassName(), "ExternalEquipment", ipAddress, sessionId))
            return tableCreator.createExternalEquipment(equipment);
        else if (wsBean.isSubclassOf(equipment.getClassName(), "Cloud", ipAddress, sessionId))
            return tableCreator.createPeering(equipment);
        else if (equipment.getClassName().toLowerCase().contains("router"))
            return tableCreator.createRouter(equipment, port);
        else if (equipment.getClassName().toLowerCase().contains("switch"))
            return tableCreator.createSwitch(equipment, port);
        else if (equipment.getClassName().toLowerCase().contains("x-connection"))
            return tableCreator.createXconection(equipment, port);
        else if (equipment.getClassName().toLowerCase().contains("odf"))
            return tableCreator.createODF(equipment, port);
        
        return null;
    }
    
    /**
     * Provides a generic way to get the parents(the GenericCommunicationsElement or 
     * the GenericBox) parents of the GenericPorts in a given path, then it parser 
     * between physical path and a simplified list, of objects connections objects
     * [Node1 - Node2]
     * [Node1 - Node3]
     * [Node3 - Node4]
     * @param path a physical path with endpoint, connection, endpoint(one or several ports, mirror, virtual, service instances)
     * @return a list of ConfigurationItem-connection-ConfigurationItem
     * @throws MetadataObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    private List<RemoteObjectLinkObject> physicalPathReader(List<RemoteObjectLight> path) 
            throws ServerSideException
    {
        RemoteObject connection  = null;
        RemoteObjectLight device = null;
        RemoteObjectLight endpoint = null;

        RemoteObjectLight sourceDevice = null;
        List<RemoteObjectLinkObject> connectionsMap = new ArrayList<>();
        //with this for we are reading the path 3 at a time, endpoint - connection -endpoint (ignoring the mirror ports)
        for (RemoteObjectLight obj : path) {
            if(wsBean.isSubclassOf(obj.getClassName(), Constants.CLASS_GENERICPHYSICALLINK, ipAddress, sessionId))
                connection = wsBean.getObject(obj.getClassName(), obj.getId(), ipAddress, sessionId);
            else if(wsBean.isSubclassOf(obj.getClassName(), Constants.CLASS_GENERICPHYSICALPORT, ipAddress, sessionId)) {
                    device = wsBean.getFirstParentOfClass(obj.getClassName(), obj.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, ipAddress, sessionId);
                //if the parent could not be found it should be aGenericCommunications element(e.g. Router, Cloud, MPLSRouter, etc)
                if(device == null)
                    device = wsBean.getFirstParentOfClass(obj.getClassName(), obj.getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME, ipAddress, sessionId);
            }
            if(sourceDevice == null){
                sourceDevice = device;
                endpoint = obj;
            }
            else if(sourceDevice.equals(device))//this is in case of mirror ports to ignore the case
                continue;
            //if we found the deviceA, connection, deviceB we are available to save the data and create connecttion set
            if(connection != null && sourceDevice != null && device != null){//TODO check what happend with unconnected things
                connectionsMap.add(new RemoteObjectLinkObject(sourceDevice, endpoint, connection, obj, device));
                connection = null;
                device = null;
                endpoint = null;
                sourceDevice = null;
            }
        }//end for
        
        return connectionsMap;
    }
}
