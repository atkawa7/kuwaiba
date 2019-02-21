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

import com.neotropic.vaadin.lienzo.client.core.shape.Point;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetClickListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.web.gui.navigation.views.AbstractScene;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.views.util.HtmlUtil;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.openide.util.Exceptions;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Adrian Fernando Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class EndToEndViewScene extends AbstractScene {
    
    public final static String VIEW_CLASS = "ServiceSimpleView"; 
    /**
     * The service this view is associated to
     */
    private RemoteObjectLight service;
    
    public EndToEndViewScene(RemoteObjectLight service, WebserviceBean wsBean, RemoteSession session) {
        super (wsBean, session);
        this.service = service;
        
        lienzoComponent.addNodeWidgetClickListener(nodeWidgetClickListener);
        lienzoComponent.addEdgeWidgetClickListener(edgeWidgetClickListener);
        
        setSizeUndefined();
    }
    
    EdgeWidgetClickListener edgeWidgetClickListener = new EdgeWidgetClickListener() {

        @Override
        public void edgeWidgetClicked(long id) {
            SrvEdgeWidget srvEdge = lienzoComponent.getEdge(id);
            Window tableInfo = new Window(" ");
            tableInfo.addStyleName("v-window-center");
//            try {
//                FormDashboardWidget formView = new FormDashboardWidget(service, wsBean, 
//                        Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
//
//                for (RemoteObjectLight edge : edges.keySet()) {
//                    if(edge.getId() == id && edge.getClassName().equals(Constants.CLASS_MPLSTUNNEL)){
//                        Component x = formView.createVC(edge);
//                        tableInfo.setContent(x);
//                        closeWindows();
//                        getUI().addWindow(tableInfo);
//                    }
//                }
//            lienzoComponent.updateEdgeWidget(srvEdge.getId());
//            } catch (ServerSideException ex) {
//                Exceptions.printStackTrace(ex);
//            }
        }
    };
    
    NodeWidgetClickListener nodeWidgetClickListener = new NodeWidgetClickListener() {

        @Override
        public void nodeWidgetClicked(long id) {
            SrvNodeWidget srvNode = lienzoComponent.getNodeWidget(id);
            Window tableInfo = new Window(" ");
            tableInfo.setId("report-forms-container");
            tableInfo.addStyleName("report-forms");
            try {
                Component x = null;
                for (RemoteObjectLight device : nodes.keySet()) {
                    if (device.getId() == id){
                        TableCreator formView = new TableCreator(service, wsBean);
                        
                        List<SrvEdgeWidget> connectedEdgeWidgets = lienzoComponent.getNodeEdgeWidgets(srvNode);
                        for(SrvEdgeWidget edge : connectedEdgeWidgets){
                            RemoteObjectLight foundEdge = findEdge(edge.getId());
                            RemoteObjectSpecialRelationships specialAttributes = wsBean.getSpecialAttributes(foundEdge.getClassName(), 
                                    foundEdge.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                            List<RemoteObjectLightList> relatedObjects = specialAttributes.getRelatedObjects();
                            List<String> relationships = specialAttributes.getRelationships();
                            for(int i = 0; i < relationships.size(); i++){
                                String relationShipName = relationships.get(i);
                                if(relationShipName.toLowerCase().contains("endpoint")){
                                    RemoteObjectLightList get = relatedObjects.get(i);
                                    RemoteObjectLight port = get.getList().get(0);
                                    if(port.getClassName().toLowerCase().contains("port") || port.getClassName().equals("Pseudowire")){
                                        List<RemoteObjectLight> parentsUntilFirstOfClass = 
                                                wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), 
                                                        device.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                        if(parentsUntilFirstOfClass.contains(device)){
                                            if(device.getClassName().toLowerCase().contains("router"))
                                                x = formView.createRouter(device, port);
                                            else if(device.getClassName().equals("ODF"))
                                                x = formView.createODF(device, port);
                                            else if(device.getClassName().toLowerCase().contains("external"))
                                                x = formView.createExternalEquipment(device);
                                            else if(device.getClassName().equals("Cloud"))
                                                x = formView.createPeering(device);
                                            tableInfo.setCaption(device.toString());
                                            tableInfo.center();
                                            HorizontalLayout lytContent = new HorizontalLayout();
                                            lytContent.setSpacing(true);
                                            lytContent.setId("report-forms-content");
                                            lytContent.addComponent(x);
                                            tableInfo.setContent(lytContent);
                                            //We close if there are any open window
                                            closeWindows();
                                            getUI().addWindow(tableInfo);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
            lienzoComponent.updateNodeWidget(id);
        }
    };
    
    @Override
    public void render(byte[] structure) throws IllegalArgumentException { 
       //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//        try {
//            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/end_to_end_view_.xml");
//            fos.write(structure);
//            fos.close();
//        } catch(Exception e) {}
        //</editor-fold>
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        
                        long nodeId = Long.valueOf(reader.getElementText());
                        SrvNodeWidget aSavedNode = findNodeWidget(nodeId);
                        if (aSavedNode != null) { //If it's null, it means that the node wasn't added by the default rendering method, so the node no longer exists and shouldn't be rendered
                            aSavedNode.setX(xCoordinate / 2); //The position is scaled (in this case to a half the original size) so they diagram can fit in a single screen 
                            aSavedNode.setY(yCoordinate / 2);
                        }
                        
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            long edgeId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
                            SrvEdgeWidget aSavedEdge = findEdgeWidget(edgeId);
                            if (aSavedEdge != null) { //If it's null, it means that the node wasn't added by the default rendering method, so the node no longer exists and shouldn't be rendered
                                List<Point> localControlPoints = new ArrayList<>();
                                while(true) {
                                    reader.nextTag();

                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")) / 2, Integer.valueOf(reader.getAttributeValue(null,"y")) / 2));
                                    } else {
                                        aSavedEdge.setControlPoints(localControlPoints);
                                        break;
                                    }
                                }
                            }
                            //lienzoComponent.updateEdgeWidget(edgeId);
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Notifications.showError(ex.getMessage());
        }

    }

    @Override
    public void render() {
        try {
            List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(service.getClassName(), 
                    service.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
            if (serviceResources.isEmpty())
                addComponent(new Label(String.format("%s does not have any resources associated to it", service)));
            else {
                for (RemoteObjectLight serviceResource : serviceResources) {
                    if (wsBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", 
                            Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId())) {
                        RemoteLogicalConnectionDetails logicalCircuitDetails = wsBean.getLogicalLinkDetails(
                                serviceResource.getClassName(), serviceResource.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                        
                        SrvNodeWidget aSideEquipmentLogicalWidget = null, bSideEquipmentLogicalWidget = null;
                        RemoteObjectLight aSideEquipmentLogical = null, bSideEquipmentLogical = null;
                        //Let's create the nodes corresponding to the endpoint A of the logical circuit
                        if(logicalCircuitDetails.getEndpointA() !=null){
                            List<RemoteObjectLight> parentsUntilFirstComEquipmentA; 
                            if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointA().getClassName(), Constants.CLASS_GENERICLOGICALPORT, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId())){
                                List<RemoteObjectLight> parentsUntilFirstPhysicalPortA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                    getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());

                                //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                                if(wsBean.isSubclassOf(parentsUntilFirstPhysicalPortA.get(0).getClassName(), "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId()))
                                    parentsUntilFirstComEquipmentA = Arrays.asList(parentsUntilFirstPhysicalPortA.get(0));
                                else
                                    parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortA.get(0).
                                        getClassName(), parentsUntilFirstPhysicalPortA.get(0).getId(), "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                            }
                            else
                                parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                    getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());

                            aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);
                            aSideEquipmentLogicalWidget = findNodeWidget(aSideEquipmentLogical);
                            if(aSideEquipmentLogicalWidget == null)
                                aSideEquipmentLogicalWidget = attachNodeWidget(aSideEquipmentLogical);
                        }
                        //Now the other side
                        if(logicalCircuitDetails.getEndpointB() !=null){
                            List<RemoteObjectLight> parentsUntilFirstComEquipmentB;
                            if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointB().getClassName(), Constants.CLASS_GENERICLOGICALPORT, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId())){
                                 List<RemoteObjectLight> parentsUntilFirstPhysicalPortB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                    getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericPhysicalPort", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                                if(wsBean.isSubclassOf(parentsUntilFirstPhysicalPortB.get(0).getClassName(), "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId()))
                                    parentsUntilFirstComEquipmentB = Arrays.asList(parentsUntilFirstPhysicalPortB.get(0)); 
                                else 
                                    parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortB.get(0).
                                        getClassName(), parentsUntilFirstPhysicalPortB.get(0).getId(), "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                            }
                            else
                                parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                    getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());

                            bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);
                            bSideEquipmentLogicalWidget = findNodeWidget(bSideEquipmentLogical);
                            if(bSideEquipmentLogicalWidget == null)
                                bSideEquipmentLogicalWidget = attachNodeWidget(bSideEquipmentLogical);
                        }
                        
                        if(aSideEquipmentLogicalWidget != null && aSideEquipmentLogical != null && bSideEquipmentLogicalWidget != null && bSideEquipmentLogical != null){
                            //Now the logical link
                            SrvEdgeWidget logicalLinkWidget = attachEdgeWidget(logicalCircuitDetails.getConnectionObject(), 
                                    aSideEquipmentLogicalWidget, 
                                    bSideEquipmentLogicalWidget);

                            logicalLinkWidget.setCaption(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                    bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName());
                        }
                        //Now we render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            int i = 2;
                            RemoteObjectLight lastAddedASidePhysicalEquipment = null;
                            if (wsBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointA().get(0).getClassName(), 
                                    Constants.CLASS_GENERICLOGICALPORT, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId()))
                                i = 3;
                            
                            for(int index = i; index < logicalCircuitDetails.getPhysicalPathForEndpointA().size(); index += 3){
                                RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointA().get(index);
                                //If the physical equipment is not a subclass of GenericCommunicationsElement, nothing will be shown.
                                RemoteObjectLight aSidePhysicalEquipment = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                        nextPhysicalHop.getId(), "ConfigurationItem", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                
                                if(aSidePhysicalEquipment != null && !aSidePhysicalEquipment.getClassName().equals("ODF"))
                                    aSidePhysicalEquipment = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                            nextPhysicalHop.getId(), "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());

                                if(aSidePhysicalEquipment == null)
                                    Notifications.showError("No communications equipment was found for this endpoint");
                                else {
                                    SrvNodeWidget aSideEquipmentPhysicalWidget = attachNodeWidget(aSidePhysicalEquipment);

                                    SrvEdgeWidget physicalLinkWidgetA = attachEdgeWidget( logicalCircuitDetails.getPhysicalPathForEndpointA().get(index - 1),
                                            findNodeWidget(index <= 3 ? aSideEquipmentLogical : lastAddedASidePhysicalEquipment), 
                                            aSideEquipmentPhysicalWidget
                                    );
                                    physicalLinkWidgetA.setCaption(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                        aSidePhysicalEquipment.getName() + ":" + nextPhysicalHop.getName());


                                    lastAddedASidePhysicalEquipment = aSidePhysicalEquipment;
                                }
                            }
                        }

                        //Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            int i = 2;
                            RemoteObjectLight lastAddedBSideEquipmentPhysical = null;
                            if (wsBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointB().get(0).getClassName(), 
                                    Constants.CLASS_GENERICLOGICALPORT, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId()))
                                i = 3;
                            for(int index = i; index < logicalCircuitDetails.getPhysicalPathForEndpointB().size(); index += 3){
                                RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointB().get(index);
                                RemoteObjectLight bSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                        nextPhysicalHop.getId(), "ConfigurationItem", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                if(bSideEquipmentPhysical != null && !bSideEquipmentPhysical.getClassName().equals("ODF"))
                                    bSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), nextPhysicalHop.getId(), 
                                            "GenericCommunicationsElement", Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                //If the equipemt physical is not a subclass of GenericCommunicationsElement, nothing will be shown.
                                if(bSideEquipmentPhysical == null)
                                    Notifications.showError("No communications equipment was found for this endpoint");

                                else {
                                    SrvNodeWidget bSideEquipmentPhysicalWidget = attachNodeWidget(bSideEquipmentPhysical);
                                     
                                    SrvEdgeWidget physicalLinkWidgetB = attachEdgeWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index - 1),
                                            findNodeWidget(index <=3 ? bSideEquipmentLogical : lastAddedBSideEquipmentPhysical),
                                            bSideEquipmentPhysicalWidget
                                    );

                                    physicalLinkWidgetB.setCaption(bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName() + " ** " +
                                        bSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());
                                    
                                    lastAddedBSideEquipmentPhysical = bSideEquipmentPhysical;
                                }
                            }
                        }
                    }
                }
                for (SrvNodeWidget node : nodes.values()) {       
                    node.setWidth(10);
                    node.setHeight(10);
                    lienzoComponent.addNodeWidget(node);
                }
                
                for (SrvEdgeWidget edge : edges.values())
                    lienzoComponent.addEdgeWidget(edge);
                
                addComponent(lienzoComponent);
            }
        } catch (ServerSideException ex) {
            clear();
            Notifications.showError(ex.getMessage());
        }
    }
    
    public void clear() {
        //this.lienzoComponent.remove
    }

    protected SrvNodeWidget attachNodeWidget(RemoteObjectLight node) {
        SrvNodeWidget newNode = new SrvNodeWidget(node.getId());
        
            
        newNode.setUrlIcon("/icons/" + node.getClassName() + ".png");

        newNode.setCaption(node.toString());
        newNode.setX(nodes.size() * 200);
        newNode.setY((nodes.size() % 2) * 200 );
        nodes.put(node, newNode);
        return newNode;
    }
    
    protected SrvEdgeWidget attachEdgeWidget(RemoteObjectLight edge, SrvNodeWidget sourceNode, SrvNodeWidget targetNode) {
        try {
            SrvEdgeWidget newEdge = new SrvEdgeWidget(edge.getId());
            newEdge.setSource(sourceNode);
            newEdge.setTarget(targetNode);
            
            RemoteClassMetadata classMetadata = wsBean.getClass(edge.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
            newEdge.setColor(HtmlUtil.toHexString(new Color(classMetadata.getColor())));
            newEdge.setCaption(edge.toString());
            edges.put(edge, newEdge);
            //lienzoComponent.addEdgeWidget(newEdge);
            return newEdge; 
        } catch (ServerSideException ex) {
            return new SrvEdgeWidget(323927373);
        }
    }
    
    private void closeWindows(){
        getUI().getWindows().forEach(currentOpenWindow -> {
            getUI().removeWindow(currentOpenWindow);
        });
    }
  
}
