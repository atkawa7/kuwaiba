/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.openide.util.Exceptions;

/**
 * This component implements the End to End view forms for SDH/MPLS services
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class FormView extends AbstractDashboardWidget{

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
    private Component divA;
    private Component divB;
    private Component divC;
    private Component divD;
    private Component divE;
    private Component divF;
    /**
     * Web service bean reference
     */
    private WebserviceBean wsBean;
    /**
     * Service reference
     */
    private RemoteObjectLight service;

    
    public FormView(RemoteObjectLight service, WebserviceBean wsBean, String ipAddress, String sessionId) throws ServerSideException {
        super(service.getName());
        this.wsBean = wsBean;
        this.service = service;
        divA = null;
        divB = null;
        divC = null;
        divD = null;
        divE = null;
        divF = null;
        
        try {
            List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(service.getClassName(), service.getId(), ipAddress, sessionId);
            if (serviceResources.isEmpty())
                addComponent(new Label(String.format("%s does not have any resources associated to it", service)));
            else {
                for (RemoteObjectLight serviceResource : serviceResources) {
                    if (wsBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", ipAddress, sessionId)) {
                        RemoteLogicalConnectionDetails logicalCircuitDetails = wsBean.getLogicalLinkDetails(
                                serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                        //Get connetcion details to create the provider's table
                        //We need the Tributary link instead of the STM
                        RemoteObjectLight sdhDelivers = wsBean.getSpecialAttribute(
                                logicalCircuitDetails.getConnectionObject().getClassName(), 
                                logicalCircuitDetails.getConnectionObject().getId(), "sdhDelivers", ipAddress, sessionId).get(0);
                        
                        RemoteObjectLight sdhTransportLink = wsBean.getSpecialAttribute(sdhDelivers.getClassName(), 
                                sdhDelivers.getId(), "sdhTransports", ipAddress, sessionId).get(0);
                        
                        RemoteObject provider = wsBean.getObject(sdhTransportLink.getClassName(), sdhTransportLink.getId(), ipAddress, sessionId);
                        String hop2NameId = provider.getAttribute("Hop2_name"); //type ProviderType
                        if(hop2NameId != null){
                            RemoteObject interLinkProvider = wsBean.getObject("ProviderType", Long.valueOf(hop2NameId), ipAddress, sessionId);
                            if(interLinkProvider != null){
                                String legalOwnerId = provider.getAttribute("Hop2LegalOwner"); //type Companies
                                RemoteObject legalOwner = wsBean.getObject("Companies", Long.valueOf(legalOwnerId), ipAddress, sessionId);
                                divC = createProviderTable(provider, legalOwner);
                            }
                        }
                        
                        String hop1NameId = provider.getAttribute("Hop1_name"); //listType ProviderType
                        if(hop1NameId != null)
                            divD = createProviderTableS(provider);
                                                
                        //Let's create the nodes corresponding to the endpoint A of the logical circuit
                        List<RemoteObjectLight> parentsUntilFirstComEquipmentA; 
                        if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointA().getClassName(), Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId)){
                            List<RemoteObjectLight> parentsUntilFirstPhysicalPortA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort", ipAddress, sessionId);

                            parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortA.get(0).
                                getClassName(), parentsUntilFirstPhysicalPortA.get(0).getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                        }
                        else
                            parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                        RemoteObjectLight aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);
                        if(wsBean.isSubclassOf(aSideEquipmentLogical.getClassName(), "GenericDataLinkElement", ipAddress, sessionId))
                           divB = createADM(aSideEquipmentLogical, logicalCircuitDetails.getEndpointA(), logicalCircuitDetails.getPhysicalPathForEndpointA(), SIDE_A);
                        else if (wsBean.isSubclassOf(aSideEquipmentLogical.getClassName(), "ExternalEquipment", ipAddress, sessionId))
                           divB =  createExternalEquipment(aSideEquipmentLogical);
                        else if (wsBean.isSubclassOf(aSideEquipmentLogical.getClassName(), "Cloud", ipAddress, sessionId))
                           divB =  createPeering(aSideEquipmentLogical);
                        else if (wsBean.isSubclassOf(aSideEquipmentLogical.getClassName(), "GenericNetworkElement", ipAddress, sessionId))
                           divB = creatRouter(aSideEquipmentLogical, logicalCircuitDetails.getEndpointA());
                        
                        //Now the other side
                        List<RemoteObjectLight> parentsUntilFirstComEquipmentB;
                        if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointB().getClassName(), Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId)){
                             List<RemoteObjectLight> parentsUntilFirstPhysicalPortB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort", ipAddress, sessionId);

                            parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortB.get(0).
                                getClassName(), parentsUntilFirstPhysicalPortB.get(0).getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                        }
                        else
                            parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                        RemoteObjectLight bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);
                        if(wsBean.isSubclassOf(bSideEquipmentLogical.getClassName(), "GenericDataLinkElement", ipAddress, sessionId))
                            divE = createADM(bSideEquipmentLogical, logicalCircuitDetails.getEndpointB(), logicalCircuitDetails.getPhysicalPathForEndpointB(), SIDE_B);
                        else if (wsBean.isSubclassOf(bSideEquipmentLogical.getClassName(), "ExternalEquipment", ipAddress, sessionId))
                            divE = createExternalEquipment(bSideEquipmentLogical);
                        else if (wsBean.isSubclassOf(bSideEquipmentLogical.getClassName(), "Cloud", ipAddress, sessionId))
                            divE = createPeering(bSideEquipmentLogical);
                        else if (wsBean.isSubclassOf(bSideEquipmentLogical.getClassName(), "GenericNetworkElement", ipAddress, sessionId))
                            divE = creatRouter(bSideEquipmentLogical, logicalCircuitDetails.getEndpointB());
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
                                
                                if(aSidePhysicalEquipment != null && aSidePhysicalEquipment.getClassName().equals("ODF")){
                                    Component odfTable = createODF(aSidePhysicalEquipment, nextPhysicalHop);
                                    if(divB != null){
                                        divA = divB; 
                                        divB = odfTable;
                                    }
                                    else
                                        divA = odfTable;
                                }
                                else if(aSidePhysicalEquipment != null && !aSidePhysicalEquipment.getClassName().equals("ODF")){
                                    aSidePhysicalEquipment = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                            nextPhysicalHop.getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                                    divA = creatRouter(aSidePhysicalEquipment, nextPhysicalHop);
                                }
                                
                                if(aSidePhysicalEquipment == null)
                                    Notifications.showError("No communications equipment was found for this endpoint");
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
                                if(bSideEquipmentPhysical != null && bSideEquipmentPhysical.getClassName().equals("ODF")){
                                   Component odfTable = createODF(bSideEquipmentPhysical, nextPhysicalHop);
                                   if(divE != null){
                                       divF = divE;
                                       divE = odfTable;
                                   }
                                }
                                
                                else if(bSideEquipmentPhysical != null && !bSideEquipmentPhysical.getClassName().equals("ODF")){
                                    bSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), nextPhysicalHop.getId(), 
                                            "GenericCommunicationsElement", ipAddress, sessionId);
                                    divF = creatRouter(bSideEquipmentPhysical, nextPhysicalHop);
                                }
                                //If the equipemt physical is not a subclass of GenericCommunicationsElement, nothing will be shown.
                                if(bSideEquipmentPhysical == null)
                                    Notifications.showError("No communications equipment was found for this endpoint");
                            }
                        }
                    }
                }
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        createContent();
    }
    
    /**
     * Creates the foot
     */
    private void createFoot(){
       HorizontalLayout lytFoot = new HorizontalLayout(new Label("This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a>", ContentMode.HTML));
       lytFoot.addStyleName("foot");
       addComponent(lytFoot);
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
     * Creates the icons for the tables
     * @param icon which icon should be load
     * @return Image with the icon
     */
    private Component createIcon(int icon){
        String path = "";
        switch(icon) {
            case ROUTER: //Router
            path = "https://149.6.144.166:8181/reports/imgs/router.png"; break;
            case ADM: //ADM
            path = "https://149.6.144.166:8181/reports/imgs/adm.png"; break;
            case ODF: //ODF
            path = "https://149.6.144.166:8181/reports/imgs/odf.png"; break;
            case PEERING: //cloud
            path = "https://149.6.144.166:8181/reports/imgs/cloud.png"; break;
            case EXTERNAL_EQUIPMENT: //External equipment
            path = "https://149.6.144.166:8181/reports/imgs/external_equipment.png"; break;
            case WACS: //WACS
            path = "https://149.6.144.166:8181/reports/imgs/logo_wacs.png"; break;
            case ORANGE: //Orange
            path = "https://149.6.144.166:8181/reports/imgs/logo_orange.png"; break;
            case PTC: //PTC
            path = "https://149.6.144.166:8181/reports/imgs/logo_ptc.png"; break;
            case ACE: //ACE
            path = "https://149.6.144.166:8181/reports/imgs/logo_ace.png"; break;
            case TATA: //TATA
            path = "https://149.6.144.166:8181/reports/imgs/logo_tata.png"; break;
            case SAT: //3SAT
            path = "https://149.6.144.166:8181/reports/imgs/logo_3sat.png"; break;
            case PCCW: //PCCW
            path = "https://149.6.144.166:8181/reports/imgs/logo_pccw.png"; break;
            case INTER: //INTERROUTE
            path = "https://149.6.144.166:8181/reports/imgs/logo_interoute.png"; break;
            default:
            path = "https://149.6.144.166:8181/report/imgs/no.png"; break;
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
     * @return a grid layout with the router's information
     */
    private Component creatRouter(RemoteObjectLight objLight, RemoteObjectLight port) throws ServerSideException{
        
        RemoteObject obj = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        //Card
        List<RemoteObjectLight> parents = wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), "GenericBoard", 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        String card = parents.get(parents.size() -1).getName();
        
        String mmr = obj.getAttribute("meetmeroom");
        String rmmr = obj.getAttribute("remotemeetmeroom");
        String rackUnits = obj.getAttribute("rackUnits");
        String rackPosition = obj.getAttribute("rackPosition");
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
        grdRouter.addComponent(createCell(getHoster(obj)), 1, 5);
        grdRouter.addComponent(createCell("DEVICE OWNER"), 0, 6);
        grdRouter.addComponent(createCell(getOwner(obj)), 1, 6);
        grdRouter.addComponent(createCell("DEVICE H&E"), 0, 7);
        grdRouter.addComponent(createCell(getHandE(obj)), 1, 7);
        
        grdRouter.addComponent(createCell(" "), 0, 8, 1, 8);
        
        grdRouter.addComponent(createCell("RACK POSITION"), 0, 9);
        grdRouter.addComponent(createCell(rackPosition), 1, 9);
        grdRouter.addComponent(createCell("RACK UNITS"), 0, 10);
        grdRouter.addComponent(createCell(rackUnits), 1, 10);
        grdRouter.addComponent(createCell("MMR"), 0, 11);
        grdRouter.addComponent(createCell(mmr), 1, 11);
        grdRouter.addComponent(createCell("RMMR"), 0, 12);
        grdRouter.addComponent(createCell(rmmr), 1, 12);

        grdRouter.addComponent(createIcon(ROUTER), 0, 13, 1, 13);
        return grdRouter;
    }
    
    /**
     * Creates a table for a Peering
     * @return a grid layout with the peering's information
     */
    private Component createPeering(RemoteObjectLight objLight) throws ServerSideException{

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
     * @return a grid layout with the ADM's information
     */
    private Component createADM(RemoteObjectLight objLight, RemoteObjectLight port, List<RemoteObjectLight> physicalPath, int side) throws ServerSideException{
        RemoteObject obj = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        
        
        String rackUnits = obj.getAttribute("rackUnits");
        String rackPosition = obj.getAttribute("rackPosition");
        String cableReference = obj.getAttribute("Cable_Reference");     
        //Card 1
        RemoteObjectLight card1 = wsBean.getParentsUntilFirstOfClass(port.getClassName(), 
                port.getId(), "IpBoard", 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
        //port 1
        RemoteObject port1 = wsBean.getObject(port.getClassName(), port.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());        
        
        //Card 2
        RemoteObjectLight card2 = wsBean.getParentsUntilFirstOfClass(physicalPath.get(0).getClassName(), 
                physicalPath.get(0).getId(), "IpBoard", 
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).get(0);
        
        //port 2
        RemoteObject port2 = wsBean.getObject(physicalPath.get(0).getClassName(), 
                physicalPath.get(0).getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        if(side == SIDE_B){
            RemoteObjectLight auxCard = card1;
            RemoteObject auxPort = port1;
            card1 = card2; 
            port1 = port2;
            port2 = auxPort;
            card2 = auxCard;
        }
        
        String portSpeed1 = port1.getAttribute("Speed_port");
        String portSpeed2 = port2.getAttribute("Speed_port");
        
        GridLayout grdADM = new GridLayout(3, 16);
        
        grdADM.addComponent(createTitle(objLight.getName(), ADM), 0, 0, 2, 0);
        
        grdADM.addComponent(createCell("CARD 1"), 0, 1);
        grdADM.addComponent(createCell("PORT 1"), 1, 1);
        grdADM.addComponent(createCell("SPEED 1"), 2, 1);
        
        //values
        grdADM.addComponent(createCell(card1.getName()), 0, 2);
        grdADM.addComponent(createCell(port.getName()), 1, 2);
        grdADM.addComponent(createCell(portSpeed1), 2, 2);
        
        grdADM.addComponent(createCell("CARD 2"), 0, 3);
        grdADM.addComponent(createCell("PORT 2"), 1, 3);
        grdADM.addComponent(createCell("SPEED 2"), 2, 3);
        //values
        grdADM.addComponent(createCell(card2.getName()), 0, 4);
        grdADM.addComponent(createCell(physicalPath.get(0).getName()), 1, 4);
        grdADM.addComponent(createCell(portSpeed2), 2, 4);
        
        grdADM.addComponent(createCell("CABLE REFERENCE"), 0, 5, 1, 5);
        grdADM.addComponent(createCell(cableReference), 2, 5);
        
        grdADM.addComponent(createCell(" "), 0, 6, 2, 6);
        
        grdADM.addComponent(createCell("DEVICE LOCATION"), 0, 7, 1, 7);
        grdADM.addComponent(createCell(getCityLocation(objLight)), 2, 7);
        grdADM.addComponent(createCell("DEVICE HOSTER"), 0, 8, 1, 8);
        grdADM.addComponent(createCell(getHoster(obj)), 2, 8);
        grdADM.addComponent(createCell("DEVICE OWNER"), 0, 9, 1, 9);
        grdADM.addComponent(createCell(getOwner(obj)), 2, 9);
        grdADM.addComponent(createCell("DEVICE H&E"), 0, 10, 1, 10);
        grdADM.addComponent(createCell(getHandE(obj)), 2, 10);
        
        grdADM.addComponent(createCell(" "), 0, 11, 2, 11);
        
        grdADM.addComponent(createCell("RACK POSITION"), 0, 12, 1,12);
        grdADM.addComponent(createCell(rackPosition), 2, 12);
        grdADM.addComponent(createCell("RACK UNITS"), 0, 13, 1, 13);
        grdADM.addComponent(createCell(rackUnits), 2, 13);
        grdADM.addComponent(createCell("MMR"), 0, 14, 1, 14);
        grdADM.addComponent(createCell("xxx"), 2, 14);

        grdADM.addComponent(createIcon(ADM), 0, 15, 2, 15);
        return grdADM;
    }
    
    /**
     * Creates a table for a ODF
     * @return a grid layout with the ODF's information
     */
    private Component createODF(RemoteObjectLight objLight, RemoteObjectLight port) throws ServerSideException{
        
        RemoteObject odf = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        String rackPostion = odf.getAttribute("rackPosition");
        String rackUnits = odf.getAttribute("rackUnits");
        
        GridLayout grdODF = new GridLayout(2, 9);
        grdODF.addComponent(createTitle("ODF", ODF), 0, 0, 1, 0);
        grdODF.addComponent(createCell("ODF-PORT"), 0, 1);
        grdODF.addComponent(createCell(" "), 0, 2, 1, 2);
        grdODF.addComponent(createCell("RACK POSTION"), 0, 3);
        grdODF.addComponent(createCell("RACK UNITS"), 0, 4);
        grdODF.addComponent(createCell(" "), 0, 5, 1, 5);
        grdODF.addComponent(createCell("DEVICE LOCATION"), 0, 6, 1, 6);
        //values
        grdODF.addComponent(createCell(port.getName()), 1, 1);
        grdODF.addComponent(createCell(rackPostion != null ? rackPostion : "Not Set"), 1, 3);
        grdODF.addComponent(createCell(rackUnits != null ? rackUnits : "Not Set"), 1, 4);

        grdODF.addComponent(createCell(createLocation(objLight)), 0, 7, 1, 7);
        
        grdODF.addComponent(createIcon(ODF), 0, 8, 1, 8);
        return grdODF;
    }
    
    /**
     * Creates a table for a provider
     * @return a grid layout with the provider's information
     */
    private Component createProviderTable(RemoteObject provider, RemoteObject legalOwner){
        
        GridLayout grdProvider = new GridLayout(2, 4);
        grdProvider.addComponent(createTitle(provider.getName(), PROVIDER), 0, 0, 1, 0);
        //Titles
        grdProvider.addComponent(createCell("PROVIDER ID"), 0, 1);
        grdProvider.addComponent(createCell("LEGAL OWNER"), 0, 2);
        //values
        grdProvider.addComponent(createCell(provider.getAttribute("Hop2id")), 1, 1);
        grdProvider.addComponent(createCell(legalOwner != null ? legalOwner.getName() : ""), 1, 2);

        grdProvider.addComponent(createIcon(selectLogo(provider.getName())), 0, 3, 1, 3);
        
        return grdProvider;
    }
    
    private int selectLogo(String providerName){
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
     * @return a grid layout with the router's information
     */
    private Component createProviderTableS(RemoteObject provider) throws ServerSideException{
        String segmentId, segment = "", endNode = "", euNode = "";
        //EuropeanNode or euNode
        String europeanNodeId = provider.getAttribute("EuropeanNode"); //listType ProviderType
        if(europeanNodeId != null)
            euNode = wsBean.getObject("NodeType", Long.valueOf(europeanNodeId),
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();//Listtype NodeType
                
        //EndNode
        String landingPointId = provider.getAttribute("LandingPoint");
        if(landingPointId != null)
            endNode = wsBean.getObject("NodeType", Long.valueOf(landingPointId), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();//Listtype NodeType

        String carfNumber = provider.getAttribute("HopCarf"); //listType ProviderType
        String hop1Name = wsBean.getObject("ProviderType", Long.valueOf(provider.getAttribute("Hop1_name")), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();
        if(selectLogo(hop1Name) == ACE){
            segmentId = provider.getAttribute("ACE_segment");
            if(segmentId != null)
                segment = wsBean.getObject("AceType", Long.valueOf(segmentId), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();
        } else if(selectLogo(hop1Name) == WACS){
            segmentId = provider.getAttribute("WACsSegment");
            if(segmentId != null)
                segment = wsBean.getObject("WacsegmentType", Long.valueOf(segmentId), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();
        }
        String moreAttributes = provider.getAttribute("More_Information");
        String hop1Id = provider.getAttribute("Hop1_id");
        String hop1LegalOwnerId = provider.getAttribute("Hop1LegalOwner");
        String hop1LegalOwner = "";
        if(hop1LegalOwnerId != null)
            hop1LegalOwner = wsBean.getObject("Companies", Long.valueOf(hop1LegalOwnerId), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();
                            
        GridLayout grdProviderSubmarineCable = new GridLayout(2, 9);
        grdProviderSubmarineCable.addComponent(createTitle(hop1Name, PROVIDER), 0, 0, 1, 0);
        
        grdProviderSubmarineCable.addComponent(createCell("LEGAL OWNER"), 0, 1);
        grdProviderSubmarineCable.addComponent(createCell("PROVIDER ID"), 0, 2);
        grdProviderSubmarineCable.addComponent(createCell("CARF NUMBER"), 0, 3);
        grdProviderSubmarineCable.addComponent(createCell("-"), 0, 4, 1, 4);
        grdProviderSubmarineCable.addComponent(createCell("EUROPEAN NODE"), 0, 5);
        grdProviderSubmarineCable.addComponent(createCell("LANDING NODE"), 0, 6);
        grdProviderSubmarineCable.addComponent(createCell("SEGMENT"), 0, 7);
        
        grdProviderSubmarineCable.addComponent(createCell(hop1Id), 1, 1);
        grdProviderSubmarineCable.addComponent(createCell(hop1LegalOwner), 1, 2);
        grdProviderSubmarineCable.addComponent(createCell(carfNumber), 1, 3);
        grdProviderSubmarineCable.addComponent(createCell(euNode), 1, 5);
        grdProviderSubmarineCable.addComponent(createCell(endNode), 1, 6);
        grdProviderSubmarineCable.addComponent(createCell(segment), 1, 7);

        grdProviderSubmarineCable.addComponent(createIcon(selectLogo(hop1Name)), 0, 8, 1, 8);
        
        return grdProviderSubmarineCable;
    }
    
    /**
     * Creates a table for a VC
     * @return a grid layout with the vc's information
     */
    private Component cretateVC(RemoteObjectLight objLight){
        GridLayout grdVC = new GridLayout(2, 3);
        grdVC.addComponent(createTitle("VC-ALGO", VC), 0, 0, 1, 0);
        
        grdVC.addComponent(createCell("PW: xxxx"), 0, 1);
        grdVC.addComponent(createCell("IP: xxxx"), 0, 2);

        grdVC.addComponent(createCell("PW: xxxx"), 1, 1);
        grdVC.addComponent(createCell("IP: xxxx"), 1, 2);
        return grdVC;
    }
    
    /**
     * Creates a table for a Switch
     * @return a grid layout with the switch's information
     */
    private Component createSwitch(RemoteObjectLight objLight){
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
     * @return a grid layout with the external equipment's information
     */
    private Component createExternalEquipment(RemoteObjectLight objLight) throws ServerSideException{
        
        RemoteObject obj = wsBean.getObject(objLight.getClassName(), objLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        
        GridLayout grdExternalEquipment = new GridLayout(2, 4);
        grdExternalEquipment.addComponent(createTitle(objLight.getName(), EXTERNAL_EQUIPMENT), 0, 0, 1, 0);
        grdExternalEquipment.addComponent(createCell("DEVICE LOCATION"), 0, 1);
        grdExternalEquipment.addComponent(createCell("DEVICE OWNER"), 0, 2);
        grdExternalEquipment.addComponent(createCell(getCityLocation(objLight)), 1, 1);
        grdExternalEquipment.addComponent(createCell(getOwner(obj)), 1, 2);
        grdExternalEquipment.addComponent(createIcon(EXTERNAL_EQUIPMENT), 0, 3, 1, 3);

        return grdExternalEquipment;
    }
    
    @Override
    public void createCover() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createContent() {
        try {        
            //We create the fom title and the state
            Label lblTitle = new Label(service.getName());
            lblTitle.setId("title");
            RemoteObject obj = wsBean.getObject(service.getClassName(), service.getId(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            //We get the service attributes
            String status = obj.getAttribute("Status"); 
            String bandwidth = obj.getAttribute("Bandwidth"); 
            //we set the form header
            HorizontalLayout lytHeader = new HorizontalLayout(lblTitle, 
                    new Label(String.format("Status: %s - Bandwidth: %s" , status != null ? status : " ", bandwidth != null ? bandwidth : " ")));
            lytHeader.setId("header");
            addComponent(lytHeader);
            HorizontalLayout lytContent = new HorizontalLayout();
            //We add the tables
            lytContent.setSpacing(true);
            lytContent.setId("content");
            if(divA != null)
                lytContent.addComponent(divA);
            if(divB != null)
                lytContent.addComponent(divB);
            if(divC != null)
                lytContent.addComponent(divC);
            if(divD != null)
                lytContent.addComponent(divD);
            if(divE != null)
                lytContent.addComponent(divE);
            if(divF != null)
                lytContent.addComponent(divF);
            addComponent(lytContent);
            this.setId("container");
            createFoot();
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
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
    
    private String getOwner(RemoteObject obj) throws ServerSideException{
        String ownerId = obj.getAttribute("LegalOwner");
        String owner = "";
        if(ownerId != null)
            owner= wsBean.getObject("Companies", Long.valueOf(ownerId),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();
        return owner;
    }
    
    private String getHoster(RemoteObject obj) throws ServerSideException{
        String hosterId = obj.getAttribute("Hoster"), hoster = "";
        if(hosterId != null)
            hoster = wsBean.getObject("Companies", Long.valueOf(hosterId),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();
        return hoster;
    }
    
    private String getHandE(RemoteObject obj) throws ServerSideException{
        String h_eId = obj.getAttribute("handsandeyes"), he = "";
        if(h_eId != null)
            he = wsBean.getObject("Companies", Long.valueOf(h_eId),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()).getName();
        return he;
    }
}
